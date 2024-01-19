package io.github.mattidragon.nodeflow.graph.node;

public enum NodeTag {
    WHITE(0xffffffff),
    RED(0xffffaaaa),
    GREEN(0xffaaffaa),
    BLUE(0xffaaaaff),
    YELLOW(0xffffffaa),
    AQUA(0xffaaffff),
    PURPLE(0xffffaaff);

    private final int color;

    NodeTag(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public String asString() {
        return name().toLowerCase();
    }

    public static NodeTag fromString(String name) {
        if (name == null) name = "";
        return switch (name) {
            case "red" -> RED;
            case "green" -> GREEN;
            case "blue" -> BLUE;
            case "yellow" -> YELLOW;
            case "aqua" -> AQUA;
            case "purple" -> PURPLE;
            default -> WHITE;
        };
    }
}
