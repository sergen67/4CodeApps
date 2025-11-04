import bcrypt from "bcryptjs";
import pkg from "@prisma/client";
const { PrismaClient } = pkg;
const prisma = new PrismaClient();

const run = async () => {
  try {
    const hashed = await bcrypt.hash("admin123", 10);
    await prisma.user.create({
      data: {
        name: "Admin",
        email: "admin@4code.com",
        password: hashed,
        role: "admin",
      },
    });
    console.log("✅ Admin hesabı başarıyla oluşturuldu.");
  } catch (err) {
    console.error("❌ Hata:", err.message);
  } finally {
    await prisma.$disconnect();
    process.exit();
  }
};

run();
