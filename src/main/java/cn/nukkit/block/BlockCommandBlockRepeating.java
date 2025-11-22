package cn.nukkit.block;

public class BlockCommandBlockRepeating extends BlockCommandBlock {

    @Override
    public int getId() {
        return REPEATING_COMMAND_BLOCK;
    }

    @Override
    public String getName() {
        return "Repeating Command Block";
    }
}
