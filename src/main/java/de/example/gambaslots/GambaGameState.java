package de.example.gambaslots;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

/** Local demo economy and slot state. No server balance or real currency is used. */
public final class GambaGameState {
    public static final int ROWS = 3;
    public static final int COLUMNS = 9;
    public static final int PAYLINE_ROW = 1;
    public static final int[] PAYLINE_COLUMNS = {3, 4, 5};

    private static final long STARTING_BALANCE = 1_000L;
    private static final Path SAVE_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("gambaslots.properties");
    private static final Random RANDOM = new Random();
    private static final List<OreSymbol> SYMBOLS = List.of(
            new OreSymbol("Kohle", Items.COAL_ORE, 2, 65),
            new OreSymbol("Eisen", Items.IRON_ORE, 3, 15),
            new OreSymbol("Gold", Items.GOLD_ORE, 5, 10),
            new OreSymbol("Diamant", Items.DIAMOND_ORE, 8, 6),
            new OreSymbol("Smaragd", Items.EMERALD_ORE, 15, 3),
            new OreSymbol("Antiker Schrott", Items.ANCIENT_DEBRIS, 30, 1)
    );

    private static final OreSymbol[][] GRID = new OreSymbol[ROWS][COLUMNS];

    private static long balance = STARTING_BALANCE;
    private static long activeBet;
    private static long spinEndsAt;
    private static int animationTicks;
    private static boolean spinning;
    private static String message = "Setze deinen Einsatz und drücke Drehen.";

    static {
        loadBalance();
        randomizeGrid();
    }

    private GambaGameState() {
    }

    public static OreSymbol getSymbol(int row, int column) {
        return GRID[row][column];
    }

    public static long getBalance() {
        return balance;
    }

    public static boolean isSpinning() {
        return spinning;
    }

    public static String getMessage() {
        return message;
    }

    public static boolean startSpin(long requestedBet) {
        if (requestedBet <= 0) {
            message = "Der Einsatz muss größer als 0 sein.";
            return false;
        }
        if (spinning) {
            message = "Die Walzen drehen bereits.";
            return false;
        }
        if (requestedBet > balance) {
            message = "Dafür hast du nicht genug Münzen.";
            return false;
        }

        balance -= requestedBet;
        activeBet = requestedBet;
        spinEndsAt = System.currentTimeMillis() + 2_000L + RANDOM.nextInt(2_001);
        animationTicks = 0;
        spinning = true;
        message = "Die Walzen drehen ...";
        saveBalance();
        return true;
    }

    /** Returns true once when the spin finishes. */
    public static boolean tick() {
        if (!spinning) {
            return false;
        }

        animationTicks++;
        if (System.currentTimeMillis() >= spinEndsAt) {
            finishSpin();
            return true;
        }

        if (animationTicks % 2 == 0) {
            randomizeGrid();
        }
        return false;
    }

    public static void setMessage(String newMessage) {
        message = newMessage;
    }

    public static long readBetForAdjustment(String input) {
        try {
            return Math.max(0L, Long.parseLong(input.trim()));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    public static long adjustBet(long current, long change) {
        try {
            return Math.max(0L, Math.addExact(current, change));
        } catch (ArithmeticException ignored) {
            return change > 0 ? Long.MAX_VALUE : 0L;
        }
    }

    public static String formatCoins(long amount) {
        return String.format(Locale.GERMANY, "%,d", amount);
    }

    private static void finishSpin() {
        randomizeGrid();
        for (int column : PAYLINE_COLUMNS) {
            GRID[PAYLINE_ROW][column] = chooseWeightedSymbol();
        }

        OreSymbol first = GRID[PAYLINE_ROW][PAYLINE_COLUMNS[0]];
        OreSymbol second = GRID[PAYLINE_ROW][PAYLINE_COLUMNS[1]];
        OreSymbol third = GRID[PAYLINE_ROW][PAYLINE_COLUMNS[2]];
        if (first.item() == second.item() && first.item() == third.item()) {
            long payout = multiplySaturated(activeBet, first.multiplier());
            balance = addSaturated(balance, payout);
            message = "Gewonnen: +" + formatCoins(payout) + " Münzen (" + first.name()
                    + " ×" + first.multiplier() + ").";
        } else {
            message = "Kein Gewinn. Der Einsatz ist verloren.";
        }

        activeBet = 0L;
        spinning = false;
        saveBalance();
    }

    private static OreSymbol chooseWeightedSymbol() {
        int totalWeight = SYMBOLS.stream().mapToInt(OreSymbol::weight).sum();
        int choice = RANDOM.nextInt(totalWeight);
        for (OreSymbol symbol : SYMBOLS) {
            choice -= symbol.weight();
            if (choice < 0) {
                return symbol;
            }
        }
        return SYMBOLS.getFirst();
    }

    private static void randomizeGrid() {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                GRID[row][column] = chooseWeightedSymbol();
            }
        }
    }

    private static long multiplySaturated(long value, int multiplier) {
        try {
            return Math.multiplyExact(value, multiplier);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static long addSaturated(long first, long second) {
        try {
            return Math.addExact(first, second);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static void loadBalance() {
        if (!Files.exists(SAVE_FILE)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(SAVE_FILE)) {
            properties.load(input);
            balance = Math.max(0L, Long.parseLong(properties.getProperty("balance", "1000")));
        } catch (IOException | NumberFormatException ignored) {
            balance = STARTING_BALANCE;
        }
    }

    private static void saveBalance() {
        Properties properties = new Properties();
        properties.setProperty("balance", Long.toString(balance));
        try {
            Files.createDirectories(SAVE_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(SAVE_FILE)) {
                properties.store(output, "GambaSlots local virtual coins");
            }
        } catch (IOException exception) {
            System.err.println("GambaSlots konnte den lokalen Kontostand nicht speichern: "
                    + exception.getMessage());
        }
    }

    public record OreSymbol(String name, Item item, int multiplier, int weight) {
    }
}
