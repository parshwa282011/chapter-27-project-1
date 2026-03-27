package poker.io;

import poker.model.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PlayerDataStore {
    private final Path file;

    public PlayerDataStore(Path file) {
        this.file = file;
    }

    public Player loadOrCreate(String name) {
        Map<String, Integer> data = readAll();
        int coins = data.getOrDefault(name, 1500);
        return new Player(name, coins);
    }

    public void save(Player player) {
        Map<String, Integer> data = readAll();
        data.put(player.getName(), player.getCoins());
        writeAll(data);
    }

    private Map<String, Integer> readAll() {
        Map<String, Integer> data = new LinkedHashMap<>();
        if (!Files.exists(file)) return data;

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length != 2) continue;
                String n = parts[0].trim();
                try {
                    int c = Integer.parseInt(parts[1].trim());
                    if (!n.isEmpty()) data.put(n, c);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
        return data;
    }

    private void writeAll(Map<String, Integer> data) {
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Integer> e : data.entrySet()) {
                bw.write(e.getKey() + "," + e.getValue());
                bw.newLine();
            }
        } catch (IOException ignored) {
        }
    }
}

