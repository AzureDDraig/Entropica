#version 150

in vec2 texCoord;

uniform sampler2D InSampler;
uniform float Time;

out vec4 fragColor;

// RGB to HSV conversion helper
vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

// HSV to RGB conversion helper
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 distortedUv = texCoord;

    // 1. Wavy Screen Edges (Sine Wave Pattern)
    // Distorts coordinates closer to the screen edges to keep the HUD/center mostly clear
    vec2 toCenter = texCoord - vec2(0.5);
    float dist = length(toCenter);
    float edgeFactor = smoothstep(0.20, 0.50, dist);

    // Apply clean sine/cosine waves for edge distortion
    float waveX = sin(texCoord.y * 35.0 + Time * 3.5) * 0.015 * edgeFactor;
    float waveY = cos(texCoord.x * 35.0 + Time * 3.5) * 0.015 * edgeFactor;
    distortedUv.x += waveX;
    distortedUv.y += waveY;

    // 2. Moving Screen Swelling & Ballooning (Swell Focal Points)
    // Rhythmic, moving centers that bulge/balloon out and return to normal
    vec2 swell1 = vec2(0.5 + sin(Time * 0.7) * 0.24, 0.5 + cos(Time * 1.0) * 0.24);
    vec2 swell2 = vec2(0.5 + cos(Time * 0.5) * 0.28, 0.5 + sin(Time * 0.8) * 0.18);

    // Swell intensities that alternate and pulse over time
    float pulse1 = 1.0 + sin(Time * 2.2) * 1.2; // Pulses between -0.2 and 2.2
    float pulse2 = 1.0 + cos(Time * 1.6) * 1.2;

    // Apply Swell 1 (ballooning distortion)
    vec2 toSwell1 = distortedUv - swell1;
    float d1 = length(toSwell1);
    if (d1 < 0.25 && pulse1 > 0.0) {
        float factor = 1.0 - (d1 / 0.25);
        distortedUv -= toSwell1 * (factor * factor) * 0.20 * pulse1;
    }

    // Apply Swell 2 (ballooning distortion)
    vec2 toSwell2 = distortedUv - swell2;
    float d2 = length(toSwell2);
    if (d2 < 0.20 && pulse2 > 0.0) {
        float factor = 1.0 - (d2 / 0.20);
        distortedUv -= toSwell2 * (factor * factor) * 0.16 * pulse2;
    }

    // 3. Sample distorted color from the main game buffer
    vec4 color = texture(InSampler, distortedUv);

    // 4. Color Wheel Rotation & Saturation Adjustments
    // Convert RGB to HSV for precise color transformations
    vec3 hsv = rgb2hsv(color.rgb);

    // Rotate colors slowly around the color wheel (hue shift)
    hsv.x = fract(hsv.x + Time * 0.08);

    // Boost the actual saturation of the colors (1.8x saturation)
    hsv.y = clamp(hsv.y * 1.8, 0.0, 1.0);

    // Convert back to RGB for final rendering output
    color.rgb = hsv2rgb(hsv);

    fragColor = color;
}
