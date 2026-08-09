import os
import math
import random
from PIL import Image

# Set seed for reproducible, high-quality pixel art patterns
random.seed(2026)

# Output directory for block textures
OUTPUT_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "common", "src", "main", "resources", "assets", "entropica", "textures", "block"
)

def torus_dist(x1, y1, x2, y2, size=32):
    dx = min(abs(x1 - x2), size - abs(x1 - x2))
    dy = min(abs(y1 - y2), size - abs(y1 - y2))
    return math.sqrt(dx*dx + dy*dy)

def generate_tiling_noise_val(x, y, freqs, size=32):
    val = 0.0
    total_w = 0.0
    for fx, fy, phase, w in freqs:
        val += math.sin(2.0 * math.pi * (fx * x + fy * y) / size + phase) * w
        total_w += w
    return val / total_w if total_w > 0 else 0.0

# ----------------------------------------------------
# Texture 1: Lesser Crystal Surface Face
# ----------------------------------------------------
def create_lesser_crystal(size=32):
    img = Image.new("RGBA", (size, size))
    pixels = img.load()
    
    freqs = [
        (1, 1, random.uniform(0, 6.28), 1.0),
        (2, 1, random.uniform(0, 6.28), 0.6),
        (1, 2, random.uniform(0, 6.28), 0.6),
        (3, 2, random.uniform(0, 6.28), 0.3),
    ]
    
    hotspots = [(8, 9), (24, 18), (14, 26)]
    
    for y in range(size):
        for x in range(size):
            base = 188.0
            cloud = generate_tiling_noise_val(x, y, freqs, size) * 9.0
            
            stria1 = math.sin(2.0 * math.pi * 3.0 * (x + y) / size)
            stria2 = math.cos(2.0 * math.pi * 4.0 * (x - y) / size)
            facet = math.sin(2.0 * math.pi * 2.0 * (x * 0.8 + y * 0.6) / size)
            lattice = (stria1 * 0.4 + stria2 * 0.3 + facet * 0.3) * 8.0
            
            refract = 0.0
            for hx, hy in hotspots:
                d = torus_dist(x, y, hx, hy, size)
                refract += math.exp(- (d**2) / (2.0 * (2.8**2))) * 14.0
                
            val = base + cloud + lattice + refract
            val_int = int(max(165, min(225, round(val))))
            pixels[x, y] = (val_int, val_int, val_int, 255)
            
    return img

# ----------------------------------------------------
# Texture 3: Greater Crystal Surface Face
# ----------------------------------------------------
def create_greater_crystal(size=32):
    img = Image.new("RGBA", (size, size))
    pixels = img.load()
    
    freqs = [
        (1, 0, random.uniform(0, 6.28), 1.0),
        (0, 1, random.uniform(0, 6.28), 1.0),
        (2, 2, random.uniform(0, 6.28), 0.7),
        (3, 1, random.uniform(0, 6.28), 0.5),
        (1, 3, random.uniform(0, 6.28), 0.5),
    ]
    
    hotspots = [(7, 7), (25, 8), (11, 23), (25, 25)]
    
    for y in range(size):
        for x in range(size):
            base = 198.0
            cloud = generate_tiling_noise_val(x, y, freqs, size) * 16.0
            
            stria1 = math.sin(2.0 * math.pi * 4.0 * (x + y) / size)
            stria2 = math.sin(2.0 * math.pi * 4.0 * (x - y) / size)
            
            r1 = math.copysign(abs(stria1)**0.6, stria1)
            r2 = math.copysign(abs(stria2)**0.6, stria2)
            facet = math.sin(2.0 * math.pi * (3.0*x - 2.0*y) / size)
            
            lattice = (r1 * 0.45 + r2 * 0.45 + facet * 0.4) * 20.0
            
            refract = 0.0
            for hx, hy in hotspots:
                d = torus_dist(x, y, hx, hy, size)
                refract += math.exp(- (d**2) / (2.0 * (2.2**2))) * 28.0
                
            val = base + cloud + lattice + refract
            val_int = int(max(140, min(255, round(val))))
            pixels[x, y] = (val_int, val_int, val_int, 255)
            
    return img

# ----------------------------------------------------
# Fracture Segment Drawing Utilities
# ----------------------------------------------------
def draw_crack_segment(pixels, p1, p2, width=1.0, val=255, max_alpha=255, size=32):
    x1, y1 = p1
    x2, y2 = p2
    
    dx = x2 - x1
    if abs(dx) > size / 2:
        x2 = x2 - size if dx > 0 else x2 + size
        
    dy = y2 - y1
    if abs(dy) > size / 2:
        y2 = y2 - size if dy > 0 else y2 + size

    dist_len = math.hypot(x2 - x1, y2 - y1)
    steps = max(int(dist_len * 4), 1)
    
    r = width / 2.0
    r_int = int(math.ceil(r))
    
    for i in range(steps + 1):
        t = i / steps
        cx = x1 + t * (x2 - x1)
        cy = y1 + t * (y2 - y1)
        
        for ox in range(-r_int, r_int + 1):
            for oy in range(-r_int, r_int + 1):
                d = math.hypot(ox, oy)
                if d <= r + 0.4:
                    px = int(round(cx + ox)) % size
                    py = int(round(cy + oy)) % size
                    
                    factor = max(0.0, min(1.0, 1.0 - (d - (r - 0.4))))
                    new_a = int(max_alpha * factor)
                    
                    if new_a > 0:
                        cur_r, cur_g, cur_b, cur_a = pixels[px, py]
                        a1 = cur_a / 255.0
                        a2 = new_a / 255.0
                        out_a = a2 + a1 * (1.0 - a2)
                        if out_a > 0:
                            out_v = int((val * a2 + cur_r * a1 * (1.0 - a2)) / out_a)
                            out_a_int = int(out_a * 255)
                            pixels[px, py] = (out_v, out_v, out_v, out_a_int)

def generate_crystal_fractures(seeds, num_branches, max_depth, branch_prob, size=32):
    segments = []
    queue = list(seeds)
    
    while queue:
        x, y, angle, depth = queue.pop(0)
        if depth > max_depth:
            continue
            
        length = random.uniform(4.0, 7.5)
        discrete_angle = round(angle / (math.pi / 4)) * (math.pi / 4)
        actual_angle = discrete_angle + random.uniform(-0.12, 0.12)
        
        nx = x + length * math.cos(actual_angle)
        ny = y + length * math.sin(actual_angle)
        
        segments.append(((x, y), (nx, ny), depth))
        
        next_angle = actual_angle + random.choice([-math.pi/4, 0, math.pi/4])
        queue.append((nx, ny, next_angle, depth + 1))
        
        if random.random() < branch_prob and depth < max_depth:
            fork_angle = actual_angle + random.choice([-math.pi/3, math.pi/3, -math.pi/2, math.pi/2])
            queue.append((nx, ny, fork_angle, depth + 1))
            
    return segments

# ----------------------------------------------------
# Texture 2: Lesser Crack Overlay
# ----------------------------------------------------
def create_lesser_cracks(size=32):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    
    seeds = [
        (3, 5, math.pi / 4, 1),
        (29, 10, 3 * math.pi / 4, 1),
        (12, 28, -math.pi / 4, 1),
        (22, 29, -3 * math.pi / 4, 1)
    ]
    
    segments = generate_crystal_fractures(seeds, num_branches=3, max_depth=3, branch_prob=0.3, size=size)
    
    for (p1, p2, depth) in segments:
        w = 1.3 if depth == 1 else 0.95
        alpha = 255 if depth == 1 else (210 if depth == 2 else 170)
        draw_crack_segment(pixels, p1, p2, width=w, val=255, max_alpha=alpha, size=size)
        
    return img

# ----------------------------------------------------
# Texture 4: Greater Crack Overlay
# ----------------------------------------------------
def create_greater_cracks(size=32):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    
    seeds = [
        (2, 4, math.pi / 4, 1),
        (30, 7, 3 * math.pi / 4, 1),
        (16, 2, math.pi / 2, 1),
        (5, 29, -math.pi / 6, 1),
        (27, 28, -5 * math.pi / 6, 1),
        (14, 16, math.pi / 3, 1)
    ]
    
    segments = generate_crystal_fractures(seeds, num_branches=6, max_depth=4, branch_prob=0.5, size=size)
    
    # Outer energetic halo
    for (p1, p2, depth) in segments:
        w_outer = 2.8 if depth <= 2 else 2.0
        draw_crack_segment(pixels, p1, p2, width=w_outer, val=235, max_alpha=140, size=size)

    # Core sharp bright white line
    for (p1, p2, depth) in segments:
        w_core = 2.1 if depth == 1 else (1.6 if depth == 2 else 1.1)
        alpha_core = 255 if depth <= 2 else 215
        draw_crack_segment(pixels, p1, p2, width=w_core, val=255, max_alpha=alpha_core, size=size)
        
    return img

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print(f"Generating 4 grayscale crystal textures to:\n  {OUTPUT_DIR}\n")
    
    textures = {
        "lesser_materia_blessing_crystal.png": create_lesser_crystal(),
        "lesser_materia_blessing_cracks.png": create_lesser_cracks(),
        "greater_materia_blessing_crystal.png": create_greater_crystal(),
        "greater_materia_blessing_cracks.png": create_greater_cracks()
    }
    
    for filename, img in textures.items():
        filepath = os.path.join(OUTPUT_DIR, filename)
        img.save(filepath)
        print(f"Saved {filename} ({img.width}x{img.height}, {img.mode})")

if __name__ == "__main__":
    main()
