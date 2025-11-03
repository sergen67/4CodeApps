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


// Login
app.post("/login", async (req, res) => {
  const { email, password } = req.body;
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user || !(await bcrypt.compare(password, user.password)))
    return res.status(401).json({ error: "Geçersiz bilgiler" });
  res.json(user);
});
app.get("/products", async (req, res) => {
  const products = await prisma.product.findMany();
  res.json(products);
});

app.post("/products", async (req, res) => {
  try {
    const { name, price, imageUrl, categoryId } = req.body;

    const product = await prisma.product.create({
      data: {
        name,
        price: parseFloat(price),
        imageUrl,
        categoryId: categoryId ? Number(categoryId) : null
      },
    });

    res.json(product);
  } catch (err) {
    console.error("Ürün ekleme hatası:", err);
    res.status(500).json({ error: err.message });
  }
});

// Ürün Güncelleme
app.put("/products/:id", async (req, res) => {
  const { role, name, price, category } = req.body;
  if (role !== "admin") return res.status(403).json({ error: "Yalnızca admin düzenleyebilir" });

  try {
    const product = await prisma.product.update({
      where: { id: parseInt(req.params.id) },
      data: { name, price, category },
    });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Ürün Silme
app.delete("/products/:id", async (req, res) => {
  const role = req.query.role;
  if (role !== "admin") return res.status(403).json({ error: "Yalnızca admin silebilir" });

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

    // 🔹 Tür dönüşümü: sayı değilse float'a çevir
    const numericTotal = parseFloat(totalPrice);
    if (isNaN(numericTotal)) {
      return res.status(400).json({ error: "Geçersiz totalPrice değeri" });
    }

    const sale = await prisma.sale.create({
      data: {
        userId: Number(userId),
        totalPrice: numericTotal,
        paymentType,
      },
    });

    res.json(sale);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});


// Tüm satışları getir (admin için)
app.get("/sales", async (req, res) => {
  try {
    const sales = await prisma.sale.findMany({
      include: { user: true },
      orderBy: { createdAt: "desc" },
    });
    res.json(sales);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Günlük ciro
app.get("/sales/daily", async (req, res) => {
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

// Aylık ciro
app.get("/sales/monthly", async (req, res) => {
  try {
    const result = await prisma.$queryRaw`
      SELECT DATE_TRUNC('month', "createdAt") as month, SUM("totalPrice") as total
      FROM "Sale"
      GROUP BY month
      ORDER BY month DESC;
    `;
    res.json(result);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});



// Orders
app.post("/orders", async (req, res) => {
  const order = await prisma.order.create({ data: req.body });
  res.json(order);
});

app.get("/orders", async (req, res) => {
  const orders = await prisma.order.findMany({ include: { user: true } });
  res.json(orders);
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Server ${PORT} portunda`));

app.get("/users", async (req, res) => {
  try {
    const users = await prisma.user.findMany();
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});
// 🔹 Kategori Listeleme
app.get("/categories", async (req, res) => {
  try {
    const categories = await prisma.category.findMany({
      include: {
        _count: {
          select: { products: true }
        }
      }
    });
    res.json(categories);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 🔹 Kategori Ekleme
app.post("/categories", async (req, res) => {
  const { name } = req.body;
  try {
    const category = await prisma.category.create({ data: { name } });
    res.json(category);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});
