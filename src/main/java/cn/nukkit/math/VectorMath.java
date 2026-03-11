package cn.nukkit.math;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author MagicDroidX
 * Nukkit Project
 */

public abstract class VectorMath {

    public static Vector2 getDirection2D(double azimuth) {
        return new Vector2(Math.cos(azimuth), Math.sin(azimuth));
    }

    record FixedVector3(Vector3 from, Vector3 to) {
        @Override
        public String toString() {
            return from.x + " " + from.y + " " + from.z + " -> " + to.x + " " + to.y + " " + to.z;
        }
    }

    public static BlockFace.Axis calculateAxis(Vector3 base, Vector3 side) {
        Vector3 vector = side.subtract(base);
        return vector.x != 0 ? BlockFace.Axis.X : vector.z != 0 ? BlockFace.Axis.Z : BlockFace.Axis.Y;
    }

    public static BlockFace calculateFace(Vector3 base, Vector3 side) {
        Vector3 vector = side.subtract(base);
        BlockFace.Axis axis = vector.x != 0 ? BlockFace.Axis.X : vector.z != 0 ? BlockFace.Axis.Z : BlockFace.Axis.Y;
        double direction = vector.getAxis(axis);
        return BlockFace.fromAxis(direction < 0 ? BlockFace.AxisDirection.NEGATIVE : BlockFace.AxisDirection.POSITIVE, axis);
    }

    public static List<Vector3> getPassByVector3(Vector3 from, Vector3 to) {
        if (from.equals(to)) {
            throw new IllegalArgumentException("from == to");
        }

        List<Vector3> result = new ArrayList<>();

        double startX = from.x;
        double startY = from.y;
        double startZ = from.z;
        double endX = to.x;
        double endY = to.y;
        double endZ = to.z;

        // Текущий воксель (целые координаты)
        int x = (int) Math.floor(startX);
        int y = (int) Math.floor(startY);
        int z = (int) Math.floor(startZ);

        // Направление шага по каждой оси
        int stepX = (int) Math.signum(endX - startX);
        int stepY = (int) Math.signum(endY - startY);
        int stepZ = (int) Math.signum(endZ - startZ);

        // Расстояния до следующей границы в параметрическом пространстве t
        double tMaxX, tMaxY, tMaxZ;
        double tDeltaX, tDeltaY, tDeltaZ;

        if (stepX != 0) {
            double nextBoundary = (stepX > 0) ? (x + 1) : x;
            tMaxX = (nextBoundary - startX) / (endX - startX);
            tDeltaX = 1.0 / Math.abs(endX - startX);
        } else {
            tMaxX = Double.POSITIVE_INFINITY;
            tDeltaX = Double.POSITIVE_INFINITY;
        }

        if (stepY != 0) {
            double nextBoundary = (stepY > 0) ? (y + 1) : y;
            tMaxY = (nextBoundary - startY) / (endY - startY);
            tDeltaY = 1.0 / Math.abs(endY - startY);
        } else {
            tMaxY = Double.POSITIVE_INFINITY;
            tDeltaY = Double.POSITIVE_INFINITY;
        }

        if (stepZ != 0) {
            double nextBoundary = (stepZ > 0) ? (z + 1) : z;
            tMaxZ = (nextBoundary - startZ) / (endZ - startZ);
            tDeltaZ = 1.0 / Math.abs(endZ - startZ);
        } else {
            tMaxZ = Double.POSITIVE_INFINITY;
            tDeltaZ = Double.POSITIVE_INFINITY;
        }

        // Начальный воксель всегда включается
        result.add(new Vector3(x, y, z));

        double t = 0;
        while (t < 1.0) {
            // Определяем, по какой оси произойдёт следующее пересечение
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                // Переход по X
                x += stepX;
                t = tMaxX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxX && tMaxY < tMaxZ) {
                // Переход по Y
                y += stepY;
                t = tMaxY;
                tMaxY += tDeltaY;
            } else if (tMaxZ < tMaxX && tMaxZ < tMaxY) {
                // Переход по Z
                z += stepZ;
                t = tMaxZ;
                tMaxZ += tDeltaZ;
            } else {
                // Случай равенства двух или трёх значений (пересечение ребра или вершины)
                // Обрабатываем все равные оси одновременно, чтобы не пропустить воксели
                final double EPS = 1e-9;
                if (Math.abs(tMaxX - tMaxY) < EPS && Math.abs(tMaxX - tMaxZ) < EPS) {
                    x += stepX;
                    y += stepY;
                    z += stepZ;
                    t = tMaxX;
                    tMaxX += tDeltaX;
                    tMaxY += tDeltaY;
                    tMaxZ += tDeltaZ;
                } else if (Math.abs(tMaxX - tMaxY) < EPS) {
                    x += stepX;
                    y += stepY;
                    t = tMaxX;
                    tMaxX += tDeltaX;
                    tMaxY += tDeltaY;
                } else if (Math.abs(tMaxX - tMaxZ) < EPS) {
                    x += stepX;
                    z += stepZ;
                    t = tMaxX;
                    tMaxX += tDeltaX;
                    tMaxZ += tDeltaZ;
                } else if (Math.abs(tMaxY - tMaxZ) < EPS) {
                    y += stepY;
                    z += stepZ;
                    t = tMaxY;
                    tMaxY += tDeltaY;
                    tMaxZ += tDeltaZ;
                } else {
                    // Защита от неопределённости – выбираем минимальное
                    double min = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
                    if (Math.abs(min - tMaxX) < EPS) {
                        x += stepX;
                        t = tMaxX;
                        tMaxX += tDeltaX;
                    } else if (Math.abs(min - tMaxY) < EPS) {
                        y += stepY;
                        t = tMaxY;
                        tMaxY += tDeltaY;
                    } else {
                        z += stepZ;
                        t = tMaxZ;
                        tMaxZ += tDeltaZ;
                    }
                }
            }

            // Добавляем новый воксель, только если пересечение произошло внутри отрезка
            var test = new Vector3(x, y, z);
            if (t <= 1.0 && !result.contains(test)) {
                result.add(test);
            }
        }

        return result;
    }
}
