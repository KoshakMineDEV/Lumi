package cn.nukkit.registry;

import cn.nukkit.item.Item;
import cn.nukkit.item.customitem.CustomItem;
import cn.nukkit.item.enchantment.*;
import cn.nukkit.item.enchantment.bow.EnchantmentBowFlame;
import cn.nukkit.item.enchantment.bow.EnchantmentBowInfinity;
import cn.nukkit.item.enchantment.bow.EnchantmentBowKnockback;
import cn.nukkit.item.enchantment.bow.EnchantmentBowPower;
import cn.nukkit.item.enchantment.crossbow.EnchantmentCrossbowMultishot;
import cn.nukkit.item.enchantment.crossbow.EnchantmentCrossbowPiercing;
import cn.nukkit.item.enchantment.crossbow.EnchantmentCrossbowQuickCharge;
import cn.nukkit.item.enchantment.damage.EnchantmentDamageAll;
import cn.nukkit.item.enchantment.damage.EnchantmentDamageArthropods;
import cn.nukkit.item.enchantment.damage.EnchantmentDamageSmite;
import cn.nukkit.item.enchantment.loot.EnchantmentLootDigging;
import cn.nukkit.item.enchantment.loot.EnchantmentLootFishing;
import cn.nukkit.item.enchantment.loot.EnchantmentLootWeapon;
import cn.nukkit.item.enchantment.mace.EnchantmentMaceBreach;
import cn.nukkit.item.enchantment.mace.EnchantmentMaceDensity;
import cn.nukkit.item.enchantment.mace.EnchantmentMaceWindBurst;
import cn.nukkit.item.enchantment.protection.*;
import cn.nukkit.item.enchantment.trident.EnchantmentTridentChanneling;
import cn.nukkit.item.enchantment.trident.EnchantmentTridentImpaling;
import cn.nukkit.item.enchantment.trident.EnchantmentTridentLoyalty;
import cn.nukkit.item.enchantment.trident.EnchantmentTridentRiptide;
import cn.nukkit.utils.DynamicClassLoader;
import cn.nukkit.utils.Identifier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.objectweb.asm.Opcodes.*;

public class EnchantmentRegistry implements IRegistry<Identifier, Enchantment, Enchantment> {

    private static final Object2ObjectOpenHashMap<Integer, Enchantment> ID_TO_ENCHANTMENT = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<Identifier, Enchantment> IDENTIFIER_TO_ENCHANTMENT = new Object2ObjectOpenHashMap<>();
    private static final AtomicBoolean isLoad = new AtomicBoolean(false);
    private static int CUSTOM_BOOK_NUMBER = 1;

    @Override
    public void init() {
        if (isLoad.getAndSet(true)) return;
        register(new EnchantmentProtectionAll());
        register(new EnchantmentProtectionFire());
        register(new EnchantmentProtectionFall());
        register(new EnchantmentProtectionExplosion());
        register(new EnchantmentProtectionProjectile());
        register(new EnchantmentThorns());
        register(new EnchantmentWaterBreath());
        register(new EnchantmentWaterWorker());
        register(new EnchantmentWaterWalker());
        register(new EnchantmentDamageAll());
        register(new EnchantmentDamageSmite());
        register(new EnchantmentDamageArthropods());
        register(new EnchantmentKnockback());
        register(new EnchantmentFireAspect());
        register(new EnchantmentLootWeapon());
        register(new EnchantmentEfficiency());
        register(new EnchantmentSilkTouch());
        register(new EnchantmentDurability());
        register(new EnchantmentLootDigging());
        register(new EnchantmentBowPower());
        register(new EnchantmentBowKnockback());
        register(new EnchantmentBowFlame());
        register(new EnchantmentBowInfinity());
        register(new EnchantmentLootFishing());
        register(new EnchantmentLure());
        register(new EnchantmentFrostWalker());
        register(new EnchantmentMending());
        register(new EnchantmentBindingCurse());
        register(new EnchantmentVanishingCurse());
        register(new EnchantmentTridentImpaling());
        register(new EnchantmentTridentLoyalty());
        register(new EnchantmentTridentRiptide());
        register(new EnchantmentTridentChanneling());
        register(new EnchantmentCrossbowMultishot());
        register(new EnchantmentCrossbowPiercing());
        register(new EnchantmentCrossbowQuickCharge());
        register(new EnchantmentSoulSpeed());
        register(new EnchantmentSwiftSneak());
        register(new EnchantmentMaceWindBurst());
        register(new EnchantmentMaceDensity());
        register(new EnchantmentMaceBreach());
    }

    public void register(Enchantment enchantment) {
        register(enchantment.getIdentifier(), enchantment);
    }

    @Override
    public void register(Identifier id, Enchantment enchantment) {
        if (IDENTIFIER_TO_ENCHANTMENT.put(id, enchantment) == null) {
            if (enchantment.getId() != EnchantmentID.CUSTOM_ENCHANTMENT_ID) {
                ID_TO_ENCHANTMENT.put(enchantment.getId(), enchantment);
            }
        } else {
            throw new RegisterException("Duplicate enchantment id " + id);
        }
    }

    public void registerCustom(Enchantment enchantment) {
        registerCustom(enchantment, true);
    }

    public void registerCustom(Enchantment enchantment, boolean registerBook) {
        Objects.requireNonNull(enchantment, "Enchantment cannot be null");
        Objects.requireNonNull(enchantment.getIdentifier(), "Identifier cannot be null");

        if (IDENTIFIER_TO_ENCHANTMENT.containsKey(enchantment.getIdentifier())) {
            throw new RegisterException("Enchantment with identifier already exists");
        }
        if (enchantment.getIdentifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            throw new RegisterException("You cannot use the minecraft namespace:");
        }

        if (registerBook) {
            registerCustomEnchantBook(enchantment);
        }

        register(enchantment.getIdentifier(), enchantment);
    }

    private void registerCustomEnchantBook(Enchantment enchantment) {
        var identifier = enchantment.getIdentifier();
        assert identifier != null;
        for (int i = 1; i <= enchantment.getMaxLevel(); i++) {
            var name = "§e%item.enchanted_book.name\n§7" + enchantment.getName() + " " + Enchantment.getLevelString(i);
            ClassWriter classWriter = new ClassWriter(0);
            MethodVisitor methodVisitor;
            String className = "CustomBookEnchanted" + CUSTOM_BOOK_NUMBER;
            classWriter.visit(V17, ACC_PUBLIC | ACC_SUPER, "cn/nukkit/item/customitem/" + className, null, "cn/nukkit/item/customitem/ItemCustomBookEnchanted", null);
            classWriter.visitSource(className + ".java", null);
            {
                methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                methodVisitor.visitCode();
                Label label0 = new Label();
                methodVisitor.visitLabel(label0);
                methodVisitor.visitLineNumber(6, label0);
                methodVisitor.visitVarInsn(ALOAD, 0);
                methodVisitor.visitLdcInsn(identifier.toString());
                methodVisitor.visitLdcInsn(name);
                methodVisitor.visitMethodInsn(INVOKESPECIAL, "cn/nukkit/item/customitem/ItemCustomBookEnchanted", "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false);
                Label label1 = new Label();
                methodVisitor.visitLabel(label1);
                methodVisitor.visitLineNumber(7, label1);
                methodVisitor.visitInsn(RETURN);
                Label label2 = new Label();
                methodVisitor.visitLabel(label2);
                methodVisitor.visitLocalVariable("this", "Lcn/nukkit/item/customitem/" + className + ";", null, label0, label2, 0);
                methodVisitor.visitMaxs(3, 1);
                methodVisitor.visitEnd();
            }
            classWriter.visitEnd();
            CUSTOM_BOOK_NUMBER++;
            try {
                @SuppressWarnings("unchecked")
                Class<? extends CustomItem> clazz = (Class<? extends CustomItem>) new DynamicClassLoader().defineClass("cn.nukkit.item.customitem." + className, classWriter.toByteArray());
                Item.registerCustomItem(clazz).assertOK();
            } catch (AssertionError e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Enchantment get(Identifier key) {
        return IDENTIFIER_TO_ENCHANTMENT.get(key);
    }

    public Enchantment get(int id) {
        return ID_TO_ENCHANTMENT.get(id);
    }

    public Map<Integer, Enchantment> getIdToEnchantment() {
        return Collections.unmodifiableMap(ID_TO_ENCHANTMENT);
    }

    public Map<Identifier, Enchantment> getIdentifierToEnchantment() {
        return Collections.unmodifiableMap(IDENTIFIER_TO_ENCHANTMENT);
    }

    @Override
    public void trim() {
        ID_TO_ENCHANTMENT.trim();
        IDENTIFIER_TO_ENCHANTMENT.trim();
    }

    @Override
    public void reload() {
        isLoad.set(false);
        ID_TO_ENCHANTMENT.clear();
        IDENTIFIER_TO_ENCHANTMENT.clear();
        init();
    }
}
