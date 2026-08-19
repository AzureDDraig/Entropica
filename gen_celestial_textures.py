import math
from PIL import Image

def generate_star_texture(size=128):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = size / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r >= 1.0:
                continue

            # Core Gaussian glow
            core = math.exp(-r * r * 14.0)

            # Outer soft halo
            halo = math.exp(-r * 3.8) * 0.45

            # 4-point diffraction spikes (subtle, beautiful cross flares)
            spike_x = math.exp(-abs(dy) * 22.0) * math.exp(-abs(dx) * 1.8) * 0.55
            spike_y = math.exp(-abs(dx) * 22.0) * math.exp(-abs(dy) * 1.8) * 0.55

            # Diagonal 4-point subtle sub-spikes
            diag1 = (dx + dy) / 1.414
            diag2 = (dx - dy) / 1.414
            sub_spike1 = math.exp(-abs(diag1) * 35.0) * math.exp(-abs(diag2) * 3.5) * 0.25
            sub_spike2 = math.exp(-abs(diag2) * 35.0) * math.exp(-abs(diag1) * 3.5) * 0.25

            total_intensity = min(1.0, core * 1.2 + halo + spike_x + spike_y + sub_spike1 + sub_spike2)
            
            # Smooth circular window at borders
            edge_fade = max(0.0, 1.0 - r)
            alpha = int(255 * total_intensity * edge_fade)

            if alpha > 0:
                # White-hot core with slight prismatic warmth at the edge
                val = 255
                pixels[x, y] = (val, val, val, alpha)

    return img

def generate_comet_head_texture(size=128):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = size / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r >= 1.0:
                continue

            # Very dense luminous nucleus
            nucleus = math.exp(-r * r * 28.0) * 1.4

            # Atmospheric expanding coma
            coma = math.exp(-r * 2.8) * 0.65

            # Soft ion shroud
            shroud = math.exp(-r * 1.6) * 0.25

            # Subtle directional head flare
            flare = math.exp(-abs(dy) * 18.0) * math.exp(-abs(dx) * 2.2) * 0.40

            intensity = min(1.0, nucleus + coma + shroud + flare)
            edge_fade = max(0.0, 1.0 - r * r)
            alpha = int(255 * intensity * edge_fade)

            if alpha > 0:
                pixels[x, y] = (255, 255, 255, alpha)

    return img

def generate_meteor_trail_texture(width=256, height=64):
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    pixels = img.load()
    half_h = (height - 1) / 2.0

    for y in range(height):
        for x in range(width):
            u = x / float(width - 1)  # 0.0 at tail -> 1.0 at head
            v = (y - half_h) / half_h # -1.0 to 1.0

            # Transverse profile: intense center, soft feathered edges
            transverse = math.exp(-v * v * 5.0)

            # Longitudinal profile: tapers and grows brighter towards head
            # Head (u=1.0) is bright, tail (u=0.0) fades to 0
            longitudinal = (u ** 1.3)

            intensity = min(1.0, transverse * longitudinal)
            alpha = int(255 * intensity)

            if alpha > 0:
                # Slight brightness curve
                c = int(230 + 25 * u)
                pixels[x, y] = (c, c, 255, alpha)

    return img

if __name__ == "__main__":
    star = generate_star_texture(128)
    star.save("c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/textures/environment/star.png")
    
    comet = generate_comet_head_texture(128)
    comet.save("c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/textures/environment/comet_head.png")

    trail = generate_meteor_trail_texture(256, 64)
    trail.save("c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/textures/environment/meteor_trail.png")
    print("Textures generated successfully!")
