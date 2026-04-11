# Design System Strategy: The Nurturing Lens

## 1. Overview & Creative North Star: "The Digital Heirloom"
This design system moves away from the clinical, data-heavy feel of traditional tracking apps. Our Creative North Star is **"The Digital Heirloom"**—a philosophy that treats every data point as a precious memory. By merging the structural logic of Material 3 Expressive with the ethereal quality of Glassmorphism, we create a UI that feels like a soft, translucent memory book.

We break the "standard app" mold by utilizing intentional asymmetry and organic layering. Instead of rigid grids, elements float and overlap like physical cutouts in a scrapbook, using depth and tonal shifts rather than lines to guide the eye.

---

## 2. Color & Atmospheric Surface
The palette is a curated selection of soft pastels designed to evoke warmth and safety. We leverage Material 3’s tonal tokens to ensure harmony while maintaining high-end editorial polish.

### The "No-Line" Rule
**Explicit Instruction:** 1px solid borders are strictly prohibited for sectioning or containment. Boundaries must be defined through:
- **Tonal Shifts:** Placing a `surface-container-lowest` card on a `surface-container` background.
- **Elevation Layers:** Using subtle backdrop blurs to separate the foreground from the background.

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. Use the following hierarchy for depth:
- **Foundation:** `surface` (#faf9f6) for the main canvas.
- **Sectioning:** `surface-container-low` (#f4f4f0) for large background groupings.
- **Interaction:** `surface-container-lowest` (#ffffff) for the most interactive, "top-level" cards.

### The "Glass & Gradient" Rule
To achieve a premium "Expressive" feel, all floating elements (Modals, FABs, Top Navigation) must use Glassmorphism:
- **Background:** `surface` at 70% opacity.
- **Effect:** `backdrop-filter: blur(24px)`.
- **Signature Textures:** Apply a linear gradient from `primary` (#825600) to `primary_container` (#ffb22d) at 15% opacity across large hero cards to provide a "golden hour" glow.

---

## 3. Typography: The Friendly Editorial
We pair **Plus Jakarta Sans** (Display/Headlines) with **Be Vietnam Pro** (Body/Labels) to balance character with legibility.

- **Display & Headlines:** Use `plusJakartaSans` with high-contrast sizing. These should feel bold and "huggable." Use `headline-lg` (2rem) for growth milestones to celebrate the moment.
- **Body & Titles:** `beVietnamPro` provides a clean, modern geometric feel that remains highly readable at smaller scales.
- **Intentional Hierarchy:** Use `display-md` (2.75rem) for primary data points (e.g., "7.2 kg") to make the baby's stats the hero of the page.

---

## 4. Elevation & Depth: Tonal Layering
Traditional shadows are replaced with "Ambient Glows" to maintain the soft, nurturing aesthetic.

- **The Layering Principle:** Depth is achieved by stacking. A `surface-container-lowest` card sitting on a `surface-container-high` background creates a natural, soft lift.
- **Ambient Shadows:** For floating elements, use a `12px` to `24px` blur with only 5% opacity. The shadow color must be derived from `on_surface` (#303330) but shifted toward the `primary` hue to avoid "dirty" grey shadows.
- **The "Ghost Border" Fallback:** If a container requires more definition, use a `1px` border using `outline-variant` (#b0b2af) at **15% opacity**. Never use a fully opaque border.

---

## 5. Signature Components

### Translucent Growth Cards
The core of the app.
- **Surface:** `surface-container-lowest` at 80% opacity with `backdrop-filter: blur(12px)`.
- **Corner Radius:** `xl` (3rem) to give a pill-like, safe feel.
- **Content:** No dividers. Use `md` (1.5rem) vertical spacing to separate time-stamps from data.

### Primary "Moments" Button
- **Background:** Gradient from `primary` (#825600) to `primary_fixed` (#ffb22d).
- **Shape:** `full` (9999px).
- **State:** On hover/press, increase the `backdrop-filter` saturation rather than darkening the color.

### Expressive Chips
- **Style:** Use `secondary_container` (#bee9ff) for selected states and `surface-container-highest` for unselected.
- **Shape:** `md` (1.5rem).
- **Constraint:** No borders; use `on_secondary_container` text color for contrast.

### Input Fields (The "Soft Entry")
- **Style:** Filled, not outlined. Use `surface_container` background.
- **Shape:** `sm` (0.5rem) on top corners, transitioning to a soft flat bottom.
- **Indicator:** Use a 2px `primary` line only when the field is focused.

### Specialized Component: The "Growth Timeline"
Instead of a straight line, use a soft, curving SVG path in `secondary` (#006786) with 20% opacity. Milestones are represented by `primary_container` circles with a soft `surface_bright` glow.

---

## 6. Do's and Don'ts

### Do
- **DO** use white space as a structural element. If in doubt, increase padding.
- **DO** overlap elements slightly (e.g., a baby's photo overlapping the edge of a glass card) to create a sense of three-dimensional space.
- **DO** use vibrant, rounded icons (Material Symbols Outlined with a 2pt stroke) to maintain the "Expressive" theme.

### Don't
- **DON'T** use pure black (#000000) for text. Always use `on_surface` (#303330) to keep the contrast "gentle."
- **DON'T** use 90-degree corners. Even the smallest elements should have at least a `sm` (0.5rem) radius.
- **DON'T** use standard Material 3 dividers. If you need to separate content, use a tonal shift or a 24dp gap.
- **DON'T** clutter the UI. Each screen should feel like a single, focused thought.