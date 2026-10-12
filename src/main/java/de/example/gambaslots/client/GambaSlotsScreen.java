package de.example.gambaslots.client;

import de.example.gambaslots.GambaGameState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class GambaSlotsScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 248;
    private static final int SLOT_SIZE = 18;

    private final List<ButtonWidget> betButtons = new ArrayList<>();
    private TextFieldWidget betField;
    private ButtonWidget spinButton;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public GambaSlotsScreen() {
        super(Text.literal("Gamba-Slotmaschine"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(PANEL_WIDTH, width - 16);
        panelHeight = Math.min(PANEL_HEIGHT, height - 12);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;

        betButtons.clear();
        int rowY = panelY + 136;
        int controlsWidth = 288;
        int left = panelX + (panelWidth - controlsWidth) / 2;
        addBetButton("-10", left, rowY, -10L);
        addBetButton("-1", left + 52, rowY, -1L);

        betField = new TextFieldWidget(textRenderer, left + 104, rowY, 80, 20, Text.literal("Einsatz"));
        betField.setMaxLength(18);
        betField.setText("10");
        betField.setPlaceholder(Text.literal("Münzen"));
        this.addDrawableChild(betField);

        addBetButton("+1", left + 188, rowY, 1L);
        addBetButton("+10", left + 240, rowY, 10L);

        spinButton = ButtonWidget.builder(Text.literal("Drehen"), button -> startSpin())
                .dimensions(panelX + (panelWidth - 120) / 2, panelY + 164, 120, 20)
                .build();
        this.addDrawableChild(spinButton);
        updateControls();
    }

    @Override
    public void tick() {
        super.tick();
        updateControls();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        drawPanel(context);
        drawSlotGrid(context);

        drawCentered(context, "Gamba", panelY + 13, 0xFFFFA8F0);
        drawCentered(context, "✦ " + GambaGameState.formatCoins(GambaGameState.getBalance()) + " Münzen ✦",
                panelY + 35, 0xFFFFE6FB);
        drawCentered(context, "Einsatz in Münzen", panelY + 119, 0xFFEBD8F2);
        drawCentered(context, GambaGameState.getMessage(), panelY + 194,
                GambaGameState.getMessage().startsWith("Gewonnen") ? 0xFF8BFF9B : 0xFFFFD5F5);
        drawCentered(context, "J öffnet/schließt  •  ESC schließt",
                panelY + panelHeight - 21, 0xFFCFB3D9);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void drawPanel(DrawContext context) {
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF21172C);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFFDB86D7);
        context.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth,
                panelY + panelHeight, 0xFF8652A4);
        context.fill(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF8652A4);
        context.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth,
                panelY + panelHeight, 0xFF8652A4);
        context.fill(panelX + 16, panelY + 51, panelX + panelWidth - 16,
                panelY + 52, 0xFF63466F);
    }

    private void drawSlotGrid(DrawContext context) {
        int gridWidth = GambaGameState.COLUMNS * SLOT_SIZE;
        int gridX = panelX + (panelWidth - gridWidth) / 2;
        int gridY = panelY + 61;

        for (int row = 0; row < GambaGameState.ROWS; row++) {
            for (int column = 0; column < GambaGameState.COLUMNS; column++) {
                int x = gridX + column * SLOT_SIZE;
                int y = gridY + row * SLOT_SIZE;
                boolean payline = row == GambaGameState.PAYLINE_ROW
                        && (column == GambaGameState.PAYLINE_COLUMNS[0]
                        || column == GambaGameState.PAYLINE_COLUMNS[1]
                        || column == GambaGameState.PAYLINE_COLUMNS[2]);

                context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, payline ? 0xFFFFD45D : 0xFF80628D);
                context.fill(x + 2, y + 2, x + SLOT_SIZE - 2, y + SLOT_SIZE - 2,
                        payline ? 0xFF392B43 : 0xFF30253A);

                GambaGameState.OreSymbol symbol = GambaGameState.getSymbol(row, column);
                if (symbol != null) {
                    context.drawItem(new ItemStack(symbol.item()), x + 1, y + 1);
                }
            }
        }
    }

    private void addBetButton(String label, int x, int y, long change) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(label), ignored -> adjustBet(change))
                .dimensions(x, y, 48, 20)
                .build();
        betButtons.add(button);
        this.addDrawableChild(button);
    }

    private void adjustBet(long change) {
        long current = GambaGameState.readBetForAdjustment(betField.getText());
        betField.setText(Long.toString(GambaGameState.adjustBet(current, change)));
        GambaGameState.setMessage("Einsatz angepasst. Drücke Drehen.");
    }

    private void startSpin() {
        String input = betField.getText().trim();
        final long amount;
        try {
            amount = Long.parseLong(input);
        } catch (NumberFormatException exception) {
            rejectBet("Bitte gib einen gültigen ganzen Münzbetrag ein.");
            return;
        }

        if (amount <= 0L) {
            rejectBet("Der Einsatz muss größer als 0 sein.");
            return;
        }

        if (!GambaGameState.startSpin(amount)) {
            rejectBet(GambaGameState.getMessage());
            return;
        }

        betField.setFocused(false);
        updateControls();
    }

    private void rejectBet(String reason) {
        GambaGameState.setMessage(reason);
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal(reason), false);
        }
    }

    private void updateControls() {
        boolean enabled = !GambaGameState.isSpinning();
        if (betField != null) {
            betField.active = enabled;
        }
        for (ButtonWidget button : betButtons) {
            button.active = enabled;
        }
        if (spinButton != null) {
            spinButton.active = enabled;
            spinButton.setMessage(Text.literal(enabled ? "Drehen" : "Dreht ..."));
        }
    }

    private void drawCentered(DrawContext context, String text, int y, int color) {
        int x = panelX + (panelWidth - textRenderer.getWidth(text)) / 2;
        context.drawTextWithShadow(textRenderer, text, x, y, color);
    }
}
