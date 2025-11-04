import pkg from "@prisma/client";
const { PrismaClient } = pkg;
const prisma = new PrismaClient();

async function main() {
  await prisma.user.deleteMany({
    where: { email: "admin@4code.com" }
  });
  console.log("🗑️ Eski admin silindi");
}

main()
  .catch((e) => console.error(e))
  .finally(async () => prisma.$disconnect());
