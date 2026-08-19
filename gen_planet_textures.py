import math
from PIL import Image

def create_planet_aethelgard(size=64):
    """Cyan Gas Giant with swirling cloud belts and Great Cyan Vortex"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r > 1.0:
                continue

            # Sphere shading (diffuse lighting from upper-left)
            z = math.sqrt(1.0 - r * r)
            # Light source at (-0.4, -0.4, 0.8)
            lx, ly, lz = -0.4, -0.4, 0.8
            l_len = math.sqrt(lx*lx + ly*ly + lz*lz)
            lx, ly, lz = lx/l_len, ly/l_len, lz/l_len
            dot = max(0.15, dx * lx + dy * ly + z * lz)

            # Gas bands
            band = math.sin(dy * 12.0 + math.cos(dx * 4.0) * 0.3) * 0.5 + 0.5
            band2 = math.cos(dy * 24.0) * 0.5 + 0.5

            # Great storm spot near lower-right
            spot_dx = dx - 0.28
            spot_dy = dy - 0.22
            spot_dist = math.sqrt(spot_dx * spot_dx * 2.0 + spot_dy * spot_dy * 4.0)
            is_spot = max(0.0, 1.0 - spot_dist / 0.25)

            # Colors: Teal (#00B4D8), Cyan (#90E0EF), Deep Navy (#0077B6), Spot (#CAF0F8)
            base_r = int((20 + 40 * band + 180 * is_spot) * dot)
            base_g = int((140 + 80 * band + 40 * band2 + 75 * is_spot) * dot)
            base_b = int((200 + 55 * band + 55 * is_spot) * dot)

            # Limb darkening
            limb = math.pow(z, 0.45)
            r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
            g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
            b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            # Anti-aliased boundary
            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            alpha = int(255 * edge_alpha)

            pixels[x, y] = (r_col, g_col, b_col, alpha)

    return img

def create_planet_cryos(size=64):
    """Pale Glacial Ice World with polar caps and ice fractures"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.18, dx * -0.45 + dy * -0.45 + z * 0.76)

            # Polar ice cap (top and bottom)
            is_polar = math.pow(abs(dy), 3.5)

            # Ice crack fractures
            crack = math.sin(dx * 16.0 + dy * 10.0) * math.cos(dy * 20.0 - dx * 8.0)
            crack_val = 1.0 if abs(crack) < 0.08 else 0.0

            # Glacial Silver & Azure (#D8F3DC, #A0E7E5, #B4F8C8, #EBFBFF)
            base_r = int((160 + 80 * is_polar + 60 * crack_val) * dot)
            base_g = int((210 + 40 * is_polar + 30 * crack_val) * dot)
            base_b = int((240 + 15 * is_polar + 15 * crack_val) * dot)

            limb = math.pow(z, 0.4)
            r_col = int(min(255, base_r * (limb * 0.65 + 0.35)))
            g_col = int(min(255, base_g * (limb * 0.65 + 0.35)))
            b_col = int(min(255, base_b * (limb * 0.65 + 0.35)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_pyroth(size=64):
    """Fiery Volcanic Rust World with Glowing Magma Rifts"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.12, dx * -0.4 + dy * -0.4 + z * 0.8)

            # Volcanic crags
            crag = math.sin(dx * 9.0 + dy * 7.0) * math.cos(dy * 11.0 - dx * 5.0)
            # Glowing magma rifts
            magma = 1.0 if abs(crag) < 0.12 else 0.0
            caldera = max(0.0, 1.0 - math.sqrt((dx + 0.2)**2 + (dy - 0.15)**2) / 0.18)

            if magma > 0.5 or caldera > 0.4:
                # Glowing hot magma (emissive, ignore diffuse shadow)
                r_col = 255
                g_col = int(120 + 80 * caldera)
                b_col = 20
            else:
                # Dark obsidian / volcanic basalt rock
                base_r = int((110 + 40 * crag) * dot)
                base_g = int((35 + 20 * crag) * dot)
                base_b = int((20 + 15 * crag) * dot)
                limb = math.pow(z, 0.4)
                r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
                g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
                b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_vespera(size=64):
    """Deep Violet/Lilac Atmosphere with Golden Cloud Ribbons"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.16, dx * -0.4 + dy * -0.4 + z * 0.8)

            bands = math.sin(dy * 10.0 + dx * 2.0) * 0.5 + 0.5
            gold_clouds = math.sin(dy * 18.0 - dx * 6.0) * math.cos(dx * 8.0)
            is_gold = 1.0 if abs(gold_clouds) < 0.15 else 0.0

            # Violet base + golden clouds
            base_r = int((140 + 70 * is_gold + 40 * bands) * dot)
            base_g = int((60 + 120 * is_gold + 20 * bands) * dot)
            base_b = int((190 - 80 * is_gold + 60 * bands) * dot)

            limb = math.pow(z, 0.45)
            r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
            g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
            b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_chronos(size=64):
    """Golden Gas Giant with warm amber atmospheric bands"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.18, dx * -0.4 + dy * -0.4 + z * 0.8)

            bands = math.sin(dy * 14.0 + math.cos(dx * 3.0) * 0.2) * 0.5 + 0.5
            bands2 = math.cos(dy * 28.0) * 0.5 + 0.5

            base_r = int((210 + 40 * bands) * dot)
            base_g = int((160 + 60 * bands + 30 * bands2) * dot)
            base_b = int((70 + 40 * bands) * dot)

            limb = math.pow(z, 0.45)
            r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
            g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
            b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

    return img

def create_planet_sylva(size=64):
    """Verdant Bioluminescent Forest World with Emerald Canopies & Glowing Mint Veins"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)
            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.14, dx * -0.4 + dy * -0.4 + z * 0.8)

            continent = math.sin(dx * 5.0 + dy * 3.5 + math.cos(dy * 7.0) * 0.6) + math.cos(dx * 9.0 - dy * 6.0) * 0.35
            is_land = continent > 0.05

            vein = math.sin(dx * 18.0 + dy * 12.0) * math.cos(dy * 20.0 - dx * 14.0)
            is_biolum = is_land and (abs(vein) < 0.09)

            if is_biolum:
                r_col = 40
                g_col = 250
                b_col = 175
            elif is_land:
                forest_shade = math.sin(dx * 14.0 + dy * 16.0) * 0.5 + 0.5
                base_r = int((20 + 30 * forest_shade) * dot)
                base_g = int((140 + 75 * forest_shade) * dot)
                base_b = int((45 + 40 * forest_shade) * dot)
                limb = math.pow(z, 0.45)
                r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
                g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
                b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))
            else:
                deep = math.sin(dx * 8.0 - dy * 10.0) * 0.5 + 0.5
                base_r = int((15 + 25 * deep) * dot)
                base_g = int((50 + 45 * deep) * dot)
                base_b = int((145 + 85 * deep) * dot)
                limb = math.pow(z, 0.45)
                r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
                g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
                b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_tartarus(size=64):
    """Heavy Iron-Silicate World with Active Sulphur Rift Volcanism & Molten Chasms"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)
            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.12, dx * -0.4 + dy * -0.4 + z * 0.8)

            crag = math.sin(dx * 8.0 + dy * 12.0 + math.cos(dx * 14.0) * 0.4) * math.cos(dy * 10.0 - dx * 6.0)
            sulfur_field = math.sin(dx * 6.0 - dy * 7.0) * 0.5 + 0.5
            rift = abs(crag) < 0.10

            caldera1 = math.sqrt((dx + 0.25)**2 + (dy - 0.2)**2)
            caldera2 = math.sqrt((dx - 0.3)**2 + (dy + 0.25)**2)
            is_caldera = (caldera1 < 0.14) or (caldera2 < 0.12)

            if rift or is_caldera:
                r_col = 255
                g_col = 150 if is_caldera else 110
                b_col = 25
            else:
                if sulfur_field > 0.65:
                    base_r = int((200 + 40 * sulfur_field) * dot)
                    base_g = int((160 + 30 * sulfur_field) * dot)
                    base_b = int((35 + 20 * sulfur_field) * dot)
                else:
                    base_r = int((85 + 30 * crag) * dot)
                    base_g = int((45 + 20 * crag) * dot)
                    base_b = int((25 + 15 * crag) * dot)
                limb = math.pow(z, 0.45)
                r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
                g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
                b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_aurelia(size=64):
    """High-Albedo Golden Core World with Shimmering Solar Belts & Specular Glint"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)
            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.20, dx * -0.4 + dy * -0.4 + z * 0.8)
            specular = math.pow(max(0.0, dx * -0.4 + dy * -0.4 + z * 0.8), 5.0) * 110.0

            bands = math.sin(dy * 16.0 + math.cos(dx * 4.0) * 0.35) * 0.5 + 0.5
            bands2 = math.cos(dy * 32.0 - dx * 6.0) * 0.5 + 0.5

            base_r = int((235 + 20 * bands) * dot + specular)
            base_g = int((195 + 45 * bands + 15 * bands2) * dot + specular * 0.9)
            base_b = int((75 + 85 * bands + 30 * bands2) * dot + specular * 0.6)

            limb = math.pow(z, 0.40)
            r_col = int(min(255, base_r * (limb * 0.65 + 0.35)))
            g_col = int(min(255, base_g * (limb * 0.65 + 0.35)))
            b_col = int(min(255, base_b * (limb * 0.65 + 0.35)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_noxus(size=64):
    """Trans-Neptunian Void Ice Dwarf with Frozen Methane Sheets & Crater Frost"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = (size - 2) / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)
            if r > 1.0:
                continue

            z = math.sqrt(1.0 - r * r)
            dot = max(0.12, dx * -0.4 + dy * -0.4 + z * 0.8)

            frost_rift = math.sin(dx * 14.0 + dy * 10.0) * math.cos(dy * 18.0 - dx * 8.0)
            is_frost = abs(frost_rift) < 0.12

            crater_dist = math.sqrt((dx - 0.2)**2 + (dy + 0.18)**2)
            is_crater = abs(crater_dist - 0.16) < 0.035

            if is_frost or is_crater:
                base_r = int((175 + 40 * is_crater) * dot)
                base_g = int((140 + 50 * is_crater) * dot)
                base_b = int((235 + 20 * is_crater) * dot)
            else:
                shade = math.sin(dy * 8.0 + dx * 6.0) * 0.5 + 0.5
                base_r = int((45 + 30 * shade) * dot)
                base_g = int((25 + 20 * shade) * dot)
                base_b = int((75 + 40 * shade) * dot)

            limb = math.pow(z, 0.45)
            r_col = int(min(255, base_r * (limb * 0.7 + 0.3)))
            g_col = int(min(255, base_g * (limb * 0.7 + 0.3)))
            b_col = int(min(255, base_b * (limb * 0.7 + 0.3)))

            edge_alpha = min(1.0, (1.0 - r) * 2.5)
            pixels[x, y] = (r_col, g_col, b_col, int(255 * edge_alpha))

    return img

def create_planet_chiron(size=64):
    """Resonant Micro-Asteroid Belt Swarm with Orbiting Chondrite Rock Bodies"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()

    asteroids = [
        (-0.08, -0.05, 15.0, 1.2),  # Primary Asteroid
        (0.40, -0.25, 8.5, 3.7),    # Secondary Asteroid
        (-0.38, 0.32, 7.0, 5.1),    # Micro Asteroid 1
        (0.22, 0.42, 6.0, 7.4),     # Micro Asteroid 2
        (-0.45, -0.35, 4.5, 9.2),   # Micro Asteroid 3
        (0.50, 0.15, 4.0, 2.5),     # Micro Asteroid 4
        (-0.15, 0.50, 3.5, 4.9),    # Micro Asteroid 5
    ]

    center = (size - 1) / 2.0
    half_size = size / 2.0

    for ast in asteroids:
        acx = center + ast[0] * half_size
        acy = center + ast[1] * half_size
        arad = ast[2]
        seed = ast[3]

        min_x = max(0, int(acx - arad - 3))
        max_x = min(size - 1, int(acx + arad + 3))
        min_y = max(0, int(acy - arad - 3))
        max_y = min(size - 1, int(acy + arad + 3))

        for y in range(min_y, max_y + 1):
            for x in range(min_x, max_x + 1):
                pdx = (x - acx)
                pdy = (y - acy)
                dist = math.sqrt(pdx * pdx + pdy * pdy)

                angle = math.atan2(pdy, pdx)
                noise_rad = arad * (1.0 + 0.18 * math.sin(angle * 4.0 + seed) + 0.12 * math.cos(angle * 7.0 - seed * 2.0))

                if dist > noise_rad:
                    continue

                norm_r = dist / noise_rad
                z = math.sqrt(max(0.0, 1.0 - norm_r * norm_r))
                ndx = pdx / noise_rad
                ndy = pdy / noise_rad

                facet = math.sin(ndx * 8.0 + ndy * 6.0 + seed) * 0.15
                dot = max(0.12, ndx * -0.45 + ndy * -0.45 + z * 0.78 + facet)

                fleck = math.sin(ndx * 15.0 - ndy * 18.0) * math.cos(ndy * 12.0)
                is_metal = abs(fleck) < 0.10

                if is_metal:
                    base_r = int(185 * dot)
                    base_g = int(195 * dot)
                    base_b = int(210 * dot)
                else:
                    base_r = int((120 + 35 * facet) * dot)
                    base_g = int((125 + 35 * facet) * dot)
                    base_b = int((135 + 35 * facet) * dot)

                edge_alpha = min(1.0, (noise_rad - dist) * 2.0)
                cur = pixels[x, y]
                new_a = int(255 * edge_alpha)
                if new_a > cur[3]:
                    pixels[x, y] = (base_r, base_g, base_b, new_a)

    return img

def create_supernova_ring(size=64):
    """Iridescent expanding shockwave ring for supernova remnants"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = img.load()
    center = (size - 1) / 2.0
    radius = size / 2.0

    for y in range(size):
        for x in range(size):
            dx = (x - center) / radius
            dy = (y - center) / radius
            r = math.sqrt(dx * dx + dy * dy)

            if r > 1.0 or r < 0.05:
                continue

            # Thin expanding ring profile around r = 0.65
            ring_dist = abs(r - 0.65)
            ring_intensity = math.exp(-ring_dist * ring_dist * 45.0)

            # Central pulsar core glow
            core_intensity = math.exp(-r * r * 35.0) * 1.3

            # Outer diffuse nebula haze
            haze = math.exp(-abs(r - 0.5) * 4.0) * 0.35

            total = min(1.0, ring_intensity + core_intensity + haze)
            alpha = int(255 * total * max(0.0, 1.0 - r))

            if alpha > 0:
                # Chromatic iridescent ring: Cyan / Lilac / White
                angle = math.atan2(dy, dx)
                col_shift = math.sin(angle * 3.0) * 0.5 + 0.5
                r_val = int(200 + 55 * col_shift)
                g_val = int(180 + 75 * (1.0 - col_shift))
                b_val = 255
                pixels[x, y] = (r_val, g_val, b_val, alpha)

    return img

if __name__ == "__main__":
    p_dir = "c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/textures/environment/"
    
    # Baseline Archon Planets (Tier 1)
    create_planet_aethelgard(64).save(p_dir + "planet_aethelgard.png")
    create_planet_cryos(64).save(p_dir + "planet_cryos.png")
    create_planet_pyroth(64).save(p_dir + "planet_pyroth.png")
    create_planet_vespera(64).save(p_dir + "planet_vespera.png")
    create_planet_chronos(64).save(p_dir + "planet_chronos.png")

    # Additive Archon Worlds & Dwarf Spheres (Tier 2 & Tier 3)
    create_planet_sylva(64).save(p_dir + "planet_sylva.png")
    create_planet_tartarus(64).save(p_dir + "planet_tartarus.png")
    create_planet_aurelia(64).save(p_dir + "planet_aurelia.png")
    create_planet_noxus(64).save(p_dir + "planet_noxus.png")
    create_planet_chiron(64).save(p_dir + "planet_chiron.png")

    # Supernova Shockwave
    create_supernova_ring(64).save(p_dir + "supernova_ring.png")
    print("All 10 Planetary & Supernova 64x64 textures generated successfully!")
