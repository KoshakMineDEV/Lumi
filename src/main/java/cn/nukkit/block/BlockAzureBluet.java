package cn.nukkit.block;

public class BlockAzureBluet extends BlockFlower {
    public BlockAzureBluet() {
        this(0);
    }

    public BlockAzureBluet(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Azure Bluet";
    }

    @Override
    public int getId() {
        return AZURE_BLUET;
    }
}
