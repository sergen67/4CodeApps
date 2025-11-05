import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import bcrypt from "bcryptjs";
import pkg from "@prisma/client";

dotenv.config();
const { PrismaClient } = pkg;
const prisma = new PrismaClient();

const app = express();
app.use(cors());
app.use(express.json());

/* ------------------ USER REGISTER ------------------ */
app.post("/register", async (req, res) => {
  try {
    const { name, email, password, role } = req.body;
    const hashed = await bcrypt.hash(password, 10);
    const user = await prisma.user.create({
      data: { name, email, password: hashed, role: role || "user" },
    });
    res.json(user);
  } catch (err) {
    console.error(err);
    res.status(400).json({ error: err.message });
  }
});

/* ------------------ LOGIN ------------------ */
app.post("/login", async (req, res) => {
  const { email, password } = req.body;
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user || !(await bcrypt.compare(password, user.password)))
    return res.status(401).json({ error: "Geçersiz bilgiler" });
  res.json(user);
});

/* ------------------ PRODUCTS ------------------ */
// ✅ Ürünleri getir (null-safe variants düzeltildi)
app.get("/products", async (req, res) => {
  try {
    const products = await prisma.product.findMany();
    res.json(
      products.map(p => ({
        ...p,
        variants: p.variants || [] // null ise boş dizi gönder
      }))
    );
  } catch (err) {
    console.error("Ürün listesi hatası:", err);
    res.status(500).json({ error: err.message });
  }
});



app.post("/products", async (req, res) => {
  try {
    const { name, price, imageUrl, categoryId, variants } = req.body;

    console.log("GELEN BODY:", req.body); // ✅ Render log’da göreceğiz

    const product = await prisma.product.create({
      data: {
        name,
        price: price ? parseFloat(price) : null,
        imageUrl: imageUrl || null,
        categoryId: categoryId ? Number(categoryId) : null,
        variants: variants ? JSON.stringify(variants) : null, // ✅ JSON olarak kaydediyoruz
      },
    });

    res.json(product);
  } catch (err) {
    console.error("❌ Ürün ekleme hatası:", err);
    res.status(500).json({ error: err.message });
  }
});



app.put("/products/:id", async (req, res) => {
  try {
    const { name, price, categoryId } = req.body;
    const product = await prisma.product.update({
      where: { id: parseInt(req.params.id) },
      data: { name, price: parseFloat(price), categoryId: Number(categoryId) },
    });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete("/products/:id", async (req, res) => {
  try {
    await prisma.product.delete({ where: { id: parseInt(req.params.id) } });
    res.json({ message: "Ürün silindi" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Satış oluşturma (çalışan yapar)
app.post("/sales", async (req, res) => {
  try {
    const { userId, totalPrice, paymentType } = req.body;

    // Doğrulama
    if (!userId || !totalPrice || !paymentType) {
      return res.status(400).json({ error: "Eksik bilgi gönderildi." });
    }

    const sale = await prisma.sale.create({
      data: {
        userId: Number(userId),
        totalPrice: parseFloat(totalPrice),
        paymentType,
      },
    });

    res.json(sale);
  } catch (err) {
    console.error("Satış oluşturulamadı:", err);
    res.status(500).json({ error: err.message });
  }
});


// Satışları listeleme (admin için)
app.get("/sales", async (req, res) => {
  try {
    const sales = await prisma.sale.findMany({
      include: {
        user: {
          select: {
            id: true,
            name: true,
            email: true,
            role: true,
          },
        },
      },
      orderBy: {
        createdAt: "desc",
      },
    });

    res.json(sales);
  } catch (err) {
    console.error("Satış listesi alınamadı:", err);
    res.status(500).json({ error: err.message });
  }
});


/* ------------------ REVENUE ------------------ */
// 🔹 Günlük ciro (bugünün toplamı)
app.get("/sales/daily", async (req, res) => {
  try {
    const result = await prisma.$queryRaw`
      SELECT DATE("createdAt") as date, SUM("totalPrice") as total
      FROM "Sale"
      WHERE DATE("createdAt") = CURRENT_DATE
      GROUP BY DATE("createdAt");
    `;
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 🔹 Haftalık ciro (son 7 gün)
app.get("/sales/weekly", async (req, res) => {
  try {
    const result = await prisma.$queryRaw`
      SELECT DATE("createdAt") as date, SUM("totalPrice") as total
      FROM "Sale"
      WHERE "createdAt" >= NOW() - INTERVAL '7 days'
      GROUP BY DATE("createdAt")
      ORDER BY date DESC;
    `;
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 🔹 Aylık ciro (son 30 gün)
app.get("/sales/monthly", async (req, res) => {
  try {
    const result = await prisma.$queryRaw`
      SELECT DATE_TRUNC('month', "createdAt") as month, SUM("totalPrice") as total
      FROM "Sale"
      WHERE "createdAt" >= NOW() - INTERVAL '30 days'
      GROUP BY month
      ORDER BY month DESC;
    `;
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


/* ------------------ ORDERS ------------------ */
app.post("/orders", async (req, res) => {
  const order = await prisma.order.create({ data: req.body });
  res.json(order);
});

app.get("/orders", async (req, res) => {
  const orders = await prisma.order.findMany({ include: { user: true } });
  res.json(orders);
});

/* ------------------ USERS ------------------ */
app.get("/users", async (req, res) => {
  try {
    const users = await prisma.user.findMany();
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/* ------------------ CATEGORIES ------------------ */
app.get("/categories", async (req, res) => {
  try {
    const categories = await prisma.category.findMany({
      include: {
        _count: { select: { products: true } },
      },
    });
    res.json(categories);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/categories", async (req, res) => {
  const { name } = req.body;
  try {
    const category = await prisma.category.create({ data: { name } });
    res.json(category);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

/* ------------------ ROOT ROUTE (RENDER TEST) ------------------ */
app.get("/", (req, res) => {
  res.send("✅ 4CodeApp backend aktif ve çalışıyor.");
});

/* ------------------ SERVER START ------------------ */
const PORT = process.env.PORT || 10000;
app.listen(PORT, () => console.log(`🚀 Server ${PORT} portunda`));
