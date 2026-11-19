package univoice.service;

import univoice.model.Feedback;
import univoice.model.FeedbackFactory;
import univoice.model.Status;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackManager {

    private final ArrayList<Feedback> records = new ArrayList<>();
    private final Path dataFile;
    private int nextId = 1;

    public FeedbackManager(String dataFileName) {
        this.dataFile = Paths.get(dataFileName);
        loadRecords();
    }

    public Feedback addFeedback(String type, String srCode, String department, String message) {
        Feedback record = FeedbackFactory.create(type, nextId++, srCode.trim(), department, message.trim());
        records.add(record);
        saveRecords();
        return record;
    }

    public Feedback findById(int id) {
        for (Feedback f : records) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    public List<Feedback> listRecords(Status statusFilter) {
        return listRecords(statusFilter, "");
    }

    public List<Feedback> searchBySrCode(String srCode) {
        return listRecords(null, srCode);
    }

    /** Filters by status and by text found in the SR-Code or Record ID (e.g. "25-06" or "UV-0003"). */
    public List<Feedback> listRecords(Status statusFilter, String srQuery) {
        String query = (srQuery == null) ? "" : srQuery.trim().toLowerCase();
        List<Feedback> result = new ArrayList<>();
        for (Feedback f : records) {
            boolean statusOk = statusFilter == null || f.getStatus() == statusFilter;
            boolean srOk = query.isEmpty() || f.getSrCode().toLowerCase().contains(query)
                    || f.getRecordId().toLowerCase().contains(query);
            if (statusOk && srOk) {
                result.add(f);
            }
        }
        return result;
    }

    public int count(Status statusFilter) {
        return listRecords(statusFilter, "").size();
    }

    public boolean updateStatus(int id, Status newStatus) {
        Feedback f = findById(id);
        if (f == null) {
            return false;
        }
        f.setStatus(newStatus);
        saveRecords();
        return true;
    }

    public boolean deleteRecord(int id) {
        Feedback f = findById(id);
        if (f == null) {
            return false;
        }
        if (!f.isResolved()) {
            throw new IllegalStateException("Only Resolved records can be deleted.");
        }
        records.remove(f);
        saveRecords();
        return true;
    }

    /**
     * Removes a troll, prank, spam, or abusive submission regardless of its
     * status. Unlike {@link #deleteRecord(int)}, this works on Pending records.
     * Each removal is written to an audit log next to the data file.
     */
    public boolean removeInvalidRecord(int id, String reason, String adminId) {
        Feedback f = findById(id);
        if (f == null) {
            return false;
        }
        records.remove(f);
        saveRecords();
        writeRemovalLog(f, reason, adminId);
        return true;
    }

    private void writeRemovalLog(Feedback f, String reason, String adminId) {
        Path log = dataFile.resolveSibling("removed_log.txt");
        String line = LocalDateTime.now().format(Feedback.DATE_FORMAT) + "|" + adminId + "|" + f.getRecordId()
                + "|" + f.getSrCode() + "|" + f.getType() + "|" + reason;
        try (BufferedWriter w = Files.newBufferedWriter(log, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(line);
            w.newLine();
        } catch (IOException e) {
            System.err.println("Could not write " + log + ": " + e.getMessage());
        }
    }

    public void loadRecords() {
        records.clear();
        if (!Files.exists(dataFile)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    records.add(FeedbackFactory.fromFileLine(line));
                } catch (IllegalArgumentException | DateTimeException e) {
                    System.err.println("Skipped a damaged record: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + dataFile + ": " + e.getMessage());
        }
        for (Feedback f : records) {
            nextId = Math.max(nextId, f.getId() + 1);
        }
    }

    public boolean saveRecords() {
        try {
            if (dataFile.getParent() != null) {
                Files.createDirectories(dataFile.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                for (Feedback f : records) {
                    writer.write(f.toFileLine());
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Could not save " + dataFile + ": " + e.getMessage());
            return false;
        }
    }
}
