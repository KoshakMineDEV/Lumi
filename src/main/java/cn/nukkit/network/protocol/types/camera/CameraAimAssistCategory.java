package cn.nukkit.network.protocol.types.camera;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Data;

import java.util.List;

@Data
public class CameraAimAssistCategory {
    private String name;
    private List<CameraAimAssistPriority> entityPriorities = new ObjectArrayList<>();
    private List<CameraAimAssistPriority> blockPriorities = new ObjectArrayList<>();
    /**
     * @since v898
     */
    private List<CameraAimAssistPriority> blockTagPriorities = new ObjectArrayList<>();
    /**
     * @since v924
     */
    private List<CameraAimAssistPriority> entityTypeFamiliesPriorities = new ObjectArrayList<>();
    private Integer entityDefaultPriorities;
    private Integer blockDefaultPriorities;
}