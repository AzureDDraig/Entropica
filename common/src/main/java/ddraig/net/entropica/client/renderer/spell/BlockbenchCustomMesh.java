package ddraig.net.entropica.client.renderer.spell;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ddraig.net.entropica.Entropica;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Runtime loader and pre-baked quad emitter for Blockbench 3D JSON geometry stored in
 * {@code assets/entropica/custom_models/<name>.json}.
 *
 * <p>Supports nested group hierarchies, 3-axis group & element Euler rotations ({@code ZYX}),
 * 1-axis element rotations, face UV rotations ({@code 0/90/180/270}), conditional sub-group
 * visibility filtering, and per-group RGB tinting (e.g., per-socket Spellgem crystal coloring).</p>
 */
public final class BlockbenchCustomMesh {

    private static final Map<String, BlockbenchCustomMesh> CACHE = new ConcurrentHashMap<>();

    public record BakedQuad(
            float[][] positions, // [4][3]
            float[][] uvs,       // [4][2]
            float nx,
            float ny,
            float nz,
            Set<String> groupLineage
    ) {
    }

    private final List<BakedQuad> quads;

    private BlockbenchCustomMesh(List<BakedQuad> quads) {
        this.quads = List.copyOf(quads);
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static BlockbenchCustomMesh getOrLoad(String modelName) {
        return CACHE.computeIfAbsent(modelName, BlockbenchCustomMesh::loadModel);
    }

    public void emit(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int light,
            int overlay,
            @Nullable Predicate<Set<String>> visibilityFilter,
            @Nullable ToIntFunction<Set<String>> rgbTintResolver
    ) {
        for (BakedQuad q : this.quads) {
            if (visibilityFilter != null && !visibilityFilter.test(q.groupLineage())) {
                continue;
            }
            int rgb = rgbTintResolver != null ? rgbTintResolver.applyAsInt(q.groupLineage()) : 0xFFFFFF;
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;

            for (int v = 0; v < 4; v++) {
                float[] p = q.positions()[v];
                float[] uv = q.uvs()[v];
                consumer.addVertex(pose, p[0], p[1], p[2])
                        .setColor(r, g, b, 1.0F)
                        .setUv(uv[0], uv[1])
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(pose, q.nx(), q.ny(), q.nz());
            }
        }
    }

    private static BlockbenchCustomMesh loadModel(String modelName) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "custom_models/" + modelName + ".json");
        try {
            Optional<Resource> resOpt = Minecraft.getInstance().getResourceManager().getResource(loc);
            if (resOpt.isEmpty()) {
                return new BlockbenchCustomMesh(List.of());
            }
            JsonObject root;
            try (InputStreamReader reader = new InputStreamReader(resOpt.get().open(), StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }

            JsonArray elements = root.has("elements") ? root.getAsJsonArray("elements") : new JsonArray();
            List<BakedQuad> baked = new ArrayList<>(elements.size() * 4);
            boolean[] visited = new boolean[elements.size()];

            if (root.has("groups") && root.get("groups").isJsonArray()) {
                walkGroupArray(
                        root.getAsJsonArray("groups"),
                        elements,
                        visited,
                        new Matrix4f(),
                        Set.of(),
                        baked
                );
            }

            // Bake any ungrouped root elements
            for (int i = 0; i < elements.size(); i++) {
                if (!visited[i]) {
                    bakeElement(elements.get(i).getAsJsonObject(), new Matrix4f(), Set.of(), baked);
                }
            }

            return new BlockbenchCustomMesh(baked);
        } catch (Exception ex) {
            Entropica.LOGGER.error("Failed to load custom Blockbench model {}: {}", loc, ex.getMessage());
            return new BlockbenchCustomMesh(List.of());
        }
    }

    private static void walkGroupArray(
            JsonArray groupArray,
            JsonArray elements,
            boolean[] visited,
            Matrix4f parentTransform,
            Set<String> parentLineage,
            List<BakedQuad> out
    ) {
        for (JsonElement entry : groupArray) {
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isNumber()) {
                int idx = entry.getAsInt();
                if (idx >= 0 && idx < elements.size() && !visited[idx]) {
                    visited[idx] = true;
                    bakeElement(elements.get(idx).getAsJsonObject(), parentTransform, parentLineage, out);
                }
            } else if (entry.isJsonObject()) {
                JsonObject groupObj = entry.getAsJsonObject();
                String name = groupObj.has("name") ? groupObj.get("name").getAsString() : "";
                Set<String> nextLineage = new LinkedHashSet<>(parentLineage);
                if (!name.isEmpty()) {
                    nextLineage.add(name);
                }
                Set<String> immutableLineage = Collections.unmodifiableSet(nextLineage);

                Matrix4f groupTransform = new Matrix4f(parentTransform);
                if (groupObj.has("rotation") && groupObj.get("rotation").isJsonArray()) {
                    JsonArray rot = groupObj.getAsJsonArray("rotation");
                    float rx = rot.get(0).getAsFloat();
                    float ry = rot.get(1).getAsFloat();
                    float rz = rot.get(2).getAsFloat();
                    float ox = 0.5F, oy = 0.5F, oz = 0.5F;
                    if (groupObj.has("origin") && groupObj.get("origin").isJsonArray()) {
                        JsonArray orig = groupObj.getAsJsonArray("origin");
                        ox = orig.get(0).getAsFloat() / 16.0F;
                        oy = orig.get(1).getAsFloat() / 16.0F;
                        oz = orig.get(2).getAsFloat() / 16.0F;
                    }
                    applyEulerZyxAroundOrigin(groupTransform, rx, ry, rz, ox, oy, oz);
                }

                if (groupObj.has("children") && groupObj.get("children").isJsonArray()) {
                    walkGroupArray(groupObj.getAsJsonArray("children"), elements, visited, groupTransform, immutableLineage, out);
                }
            }
        }
    }

    private static void bakeElement(
            JsonObject elem,
            Matrix4f groupTransform,
            Set<String> lineage,
            List<BakedQuad> out
    ) {
        if (!elem.has("from") || !elem.has("to") || !elem.has("faces")) {
            return;
        }
        JsonArray fromArr = elem.getAsJsonArray("from");
        JsonArray toArr = elem.getAsJsonArray("to");
        float x1 = fromArr.get(0).getAsFloat() / 16.0F;
        float y1 = fromArr.get(1).getAsFloat() / 16.0F;
        float z1 = fromArr.get(2).getAsFloat() / 16.0F;
        float x2 = toArr.get(0).getAsFloat() / 16.0F;
        float y2 = toArr.get(1).getAsFloat() / 16.0F;
        float z2 = toArr.get(2).getAsFloat() / 16.0F;

        Matrix4f elemTransform = new Matrix4f(groupTransform);
        if (elem.has("rotation") && elem.get("rotation").isJsonObject()) {
            JsonObject rot = elem.getAsJsonObject("rotation");
            float ox = 0.5F, oy = 0.5F, oz = 0.5F;
            if (rot.has("origin") && rot.get("origin").isJsonArray()) {
                JsonArray orig = rot.getAsJsonArray("origin");
                ox = orig.get(0).getAsFloat() / 16.0F;
                oy = orig.get(1).getAsFloat() / 16.0F;
                oz = orig.get(2).getAsFloat() / 16.0F;
            }
            if (rot.has("x") || rot.has("y") || rot.has("z")) {
                float rx = rot.has("x") ? rot.get("x").getAsFloat() : 0.0F;
                float ry = rot.has("y") ? rot.get("y").getAsFloat() : 0.0F;
                float rz = rot.has("z") ? rot.get("z").getAsFloat() : 0.0F;
                applyEulerZyxAroundOrigin(elemTransform, rx, ry, rz, ox, oy, oz);
            } else if (rot.has("angle") && rot.has("axis")) {
                float angle = rot.get("angle").getAsFloat();
                if (Math.abs(angle) > 1.0E-5F) {
                    String axis = rot.get("axis").getAsString();
                    float rad = (float) Math.toRadians(angle);
                    elemTransform.translate(ox, oy, oz);
                    switch (axis) {
                        case "x" -> elemTransform.rotateX(rad);
                        case "y" -> elemTransform.rotateY(rad);
                        case "z" -> elemTransform.rotateZ(rad);
                    }
                    elemTransform.translate(-ox, -oy, -oz);
                }
            }
        }

        Matrix3f normalMat = new Matrix3f(elemTransform);
        normalMat.invert().transpose();

        JsonObject faces = elem.getAsJsonObject("faces");
        for (Map.Entry<String, JsonElement> entry : faces.entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            String dir = entry.getKey();
            JsonObject faceObj = entry.getValue().getAsJsonObject();

            float u1 = 0.0F, v1 = 0.0F, u2 = 1.0F, v2 = 1.0F;
            if (faceObj.has("uv") && faceObj.get("uv").isJsonArray()) {
                JsonArray uvArr = faceObj.getAsJsonArray("uv");
                u1 = uvArr.get(0).getAsFloat() / 16.0F;
                v1 = uvArr.get(1).getAsFloat() / 16.0F;
                u2 = uvArr.get(2).getAsFloat() / 16.0F;
                v2 = uvArr.get(3).getAsFloat() / 16.0F;
            }
            int uvRot = faceObj.has("rotation") ? faceObj.get("rotation").getAsInt() : 0;

            float[][] rawVerts;
            Vector3f rawNormal;
            switch (dir) {
                case "north" -> {
                    rawVerts = new float[][]{
                            {x2, y2, z1},
                            {x2, y1, z1},
                            {x1, y1, z1},
                            {x1, y2, z1}
                    };
                    rawNormal = new Vector3f(0.0F, 0.0F, -1.0F);
                }
                case "south" -> {
                    rawVerts = new float[][]{
                            {x1, y2, z2},
                            {x1, y1, z2},
                            {x2, y1, z2},
                            {x2, y2, z2}
                    };
                    rawNormal = new Vector3f(0.0F, 0.0F, 1.0F);
                }
                case "west" -> {
                    rawVerts = new float[][]{
                        {x1, y2, z1},
                        {x1, y1, z1},
                        {x1, y1, z2},
                        {x1, y2, z2}
                    };
                    rawNormal = new Vector3f(-1.0F, 0.0F, 0.0F);
                }
                case "east" -> {
                    rawVerts = new float[][]{
                            {x2, y2, z2},
                            {x2, y1, z2},
                            {x2, y1, z1},
                            {x2, y2, z1}
                    };
                    rawNormal = new Vector3f(1.0F, 0.0F, 0.0F);
                }
                case "up" -> {
                    rawVerts = new float[][]{
                            {x1, y2, z1},
                            {x1, y2, z2},
                            {x2, y2, z2},
                            {x2, y2, z1}
                    };
                    rawNormal = new Vector3f(0.0F, 1.0F, 0.0F);
                }
                case "down" -> {
                    rawVerts = new float[][]{
                            {x1, y1, z2},
                            {x1, y1, z1},
                            {x2, y1, z1},
                            {x2, y1, z2}
                    };
                    rawNormal = new Vector3f(0.0F, -1.0F, 0.0F);
                }
                default -> {
                    continue;
                }
            }

            float[][] baseUvs = {
                    {u1, v1},
                    {u1, v2},
                    {u2, v2},
                    {u2, v1}
            };
            int shift = ((uvRot / 90) % 4 + 4) % 4;
            float[][] rotatedUvs = new float[4][2];
            for (int i = 0; i < 4; i++) {
                rotatedUvs[i] = baseUvs[(i + 4 - shift) % 4];
            }

            float[][] transformedVerts = new float[4][3];
            Vector4f tmp = new Vector4f();
            for (int i = 0; i < 4; i++) {
                tmp.set(rawVerts[i][0], rawVerts[i][1], rawVerts[i][2], 1.0F);
                elemTransform.transform(tmp);
                transformedVerts[i][0] = tmp.x;
                transformedVerts[i][1] = tmp.y;
                transformedVerts[i][2] = tmp.z;
            }

            normalMat.transform(rawNormal);
            if (rawNormal.lengthSquared() > 1.0E-6F) {
                rawNormal.normalize();
            }

            out.add(new BakedQuad(transformedVerts, rotatedUvs, rawNormal.x, rawNormal.y, rawNormal.z, lineage));
        }
    }

    private static void applyEulerZyxAroundOrigin(
            Matrix4f mat,
            float rxDeg,
            float ryDeg,
            float rzDeg,
            float ox,
            float oy,
            float oz
    ) {
        if (Math.abs(rxDeg) < 1.0E-5F && Math.abs(ryDeg) < 1.0E-5F && Math.abs(rzDeg) < 1.0E-5F) {
            return;
        }
        mat.translate(ox, oy, oz);
        if (Math.abs(rzDeg) > 1.0E-5F) mat.rotateZ((float) Math.toRadians(rzDeg));
        if (Math.abs(ryDeg) > 1.0E-5F) mat.rotateY((float) Math.toRadians(ryDeg));
        if (Math.abs(rxDeg) > 1.0E-5F) mat.rotateX((float) Math.toRadians(rxDeg));
        mat.translate(-ox, -oy, -oz);
    }
}
