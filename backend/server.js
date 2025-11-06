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

/* ------------------ REGISTER ------------------ */
app.post("/register", async (req, res) => {
  try {
    const { name, email, password, role } = req.body;
    const hashed = await bcrypt.hash(password, 10);
    const user = await prisma.user.create({
      data: { name, email, password: hashed, role: role || "employee" },
    });
    res.json(user);
  } catch (err) {
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

/* ------------------ PRODUCT ADD ------------------ */
app.post("/products", async (req, res) => {
  try {
    const { name, price, imageUrl, categoryId, variants } = req.body;

    const product = await prisma.product.create({
      data: {
        name,
        price: price ? parseFloat(price) : 0,
        imageUrl: imageUrl || null,
        categoryId: categoryId ? Number(categoryId) : null,
        // ✅ JSON alanı olduğu için string'e çevirme YOK
        variants: variants ? variants : [],
      },
    });

    res.json(product);
  } catch (err) {
    console.error("❌ Ürün ekleme hatası:", err);
    res.status(500).json({ error: err.message });
  }
});

/* ------------------ PRODUCT LIST ------------------ */
app.get("/products", async (req, res) => {
  try {
    const products = await prisma.product.findMany({
      include: { category: true },
    });

    // ✅ Eğer JSON verisi string olarak saklanmışsa çözümle
    res.json(
      products.map((p) => ({
        id: p.id,
        name: p.name,
        price: p.price,
        imageUrl: p.imageUrl,
        categoryId: p.categoryId,
        category: p.category?.name || null,
        variants:
          typeof p.variants === "string"
            ? JSON.parse(p.variants)
            : p.variants || [],
        createdAt: p.createdAt,
      }))
    );
  } catch (err) {
    console.error("Ürün listesi hatası:", err);
    res.status(500).json({ error: err.message });
  }
});
// 🔹 Ürün Güncelleme
app.put("/products/:id", async (req, res) => {
  try {
    const { id } = req.params;
    const { name, price, variants } = req.body;

    const updated = await prisma.product.update({
      where: { id: parseInt(id) },
      data: {
        name: name,
        price: price,
        variants: variants ? JSON.stringify(variants) : undefined
      }
    });

    res.json({ success: true, updated });
  } catch (err) {
    console.error("❌ Ürün güncelleme hatası:", err);
    res.status(500).json({ error: "Güncelleme başarısız" });
  }
});

/* ------------------ SALES ------------------ */
app.post("/sales", async (req, res) => {
  try {
    const { userId, totalPrice, paymentType } = req.body;
    console.log("📩 Gelen satış verisi:", req.body);

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


app.get("/sales", async (req, res) => {
  try {
    const { start, end } = req.query;

    const where = {};
    if (start && end) {
      where.createdAt = {
        gte: new Date(`${start}T00:00:00Z`),
        lte: new Date(`${end}T23:59:59Z`),
      };
    }

    const sales = await prisma.sale.findMany({
      where,
      include: { user: { select: { id: true, name: true } } },
      orderBy: { createdAt: "desc" },
    });

    res.json(sales);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});



/* ------------------ CATEGORIES ------------------ */
app.get("/categories", async (req, res) => {
  try {
    const categories = await prisma.category.findMany();
    res.json(categories);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/categories", async (req, res) => {
  try {
    const category = await prisma.category.create({
      data: { name: req.body.name },
    });
    res.json(category);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/* ------------------ REVENUE ------------------ */
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
app.get("/sales/weekly", async (req, res) => {
  try {
    const result = await prisma.$queryRaw`
      SELECT SUM("totalPrice") AS total
      FROM "Sale"
      WHERE "createdAt" >= NOW() - INTERVAL '7 day'
    `;
    res.json(result);
  } catch (err) {
    console.error("Haftalık ciro hatası:", err);
    res.status(500).json({ error: err.message });
  }
});
app.get("/sales/monthly", async (req, res) => {
  try {
    const result = await prisma.$queryRaw`
      SELECT SUM("totalPrice") AS total
      FROM "Sale"
      WHERE "createdAt" >= NOW() - INTERVAL '30 day'
    `;
    res.json(result);
  } catch (err) {
    console.error("Aylık ciro hatası:", err);
    res.status(500).json({ error: err.message });
  }
});

// ✅ Root route - Render test
app.get("/", (req, res) => {
  res.send("✅ 4CodeApp backend aktif ve çalışıyor.");
});
app.get("/users", async (req, res) => {
  try {
    const users = await prisma.user.findMany();
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});
/* ------------------ USER DELETE ------------------ */
app.delete("/users/:id", async (req, res) => {
  // Sadece 'admin' rolündeki kullanıcıların silme işlemi yapabilmesi için kontrol
  if (req.query.role !== 'admin') {
    return res.status(403).json({ error: "Bu işlemi yapmaya yetkiniz yok." });
  }

  try {
    const { id } = req.params;
    await prisma.user.delete({
      where: { id: parseInt(id) },
    });
    res.json({ message: "Kullanıcı başarıyla silindi." });
  } catch (err) {
    console.error("❌ Kullanıcı silme hatası:", err);
    // Prisma'nın P2025 hatası, kaydın bulunamadığını belirtir.
    if (err.code === 'P2025') {
       return res.status(404).json({ error: "Silinecek kullanıcı bulunamadı." });
    }
    res.status(500).json({ error: err.message });
  }
});
/* ------------------ SERVER ------------------ */
const PORT = process.env.PORT || 10000;
app.listen(PORT, () => console.log(`🚀 Server ${PORT} portunda`));
