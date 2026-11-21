package id.edu.homeschool.ui;

import id.edu.homeschool.model.StudySession;
import id.edu.homeschool.model.Subject;
import id.edu.homeschool.service.LearningWorkspaceService;
import id.edu.homeschool.ui.design.EmptyState;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Daily or weekly totals of the durations recorded for scheduled study sessions. */
final class StudySessionDurationChart extends JPanel {
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id"));
    private static final DateTimeFormatter WEEK_LABEL = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id"));

    private final Workspace workspace;
    private final JComboBox<SubjectOption> subjectFilter = Ui.combo();
    private final JComboBox<PeriodOption> periodFilter = Ui.combo();
    private final JPanel plotHolder = new JPanel(new BorderLayout());
    private final JLabel summary = Ui.muted("Durasi sesi tersimpan ditampilkan sebagai durasi terjadwal.");
    private final DurationPlot plot = new DurationPlot();
    private List<StudySession> visibleSessions = List.of();
    private List<Bucket> buckets = List.of();
    private boolean updatingFilters;

    StudySessionDurationChart(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_2));
        this.workspace = workspace;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.SPACE_3, 0));

        JPanel card = Ui.card();
        card.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_3, Theme.SPACE_4,
                Theme.SPACE_2, Theme.SPACE_4));
        JPanel heading = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        heading.setOpaque(false);
        JPanel titles = new JPanel();
        titles.setLayout(new javax.swing.BoxLayout(titles, javax.swing.BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel title = Ui.label("Durasi sesi belajar");
        title.setFont(Theme.FONT_14_SEMIBOLD);
        JLabel subtitle = Ui.subtle("Durasi sesi terjadwal.");
        subtitle.setFont(Theme.FONT_11);
        titles.add(title);
        titles.add(subtitle);

        subjectFilter.setPreferredSize(new Dimension(190, Theme.CONTROL_HEIGHT));
        subjectFilter.addActionListener(event -> {
            if (!updatingFilters) refresh();
        });
        periodFilter.setPreferredSize(new Dimension(140, Theme.CONTROL_HEIGHT));
        periodFilter.addItem(new PeriodOption("7 hari", 7, false));
        periodFilter.addItem(new PeriodOption("30 hari", 30, false));
        periodFilter.addItem(new PeriodOption("12 minggu", 84, true));
        periodFilter.setSelectedIndex(1);
        periodFilter.addActionListener(event -> {
            if (!updatingFilters) refresh();
        });

        JPanel filters = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
        filters.setOpaque(false);
        filters.add(subjectFilter);
        filters.add(periodFilter);
        heading.add(titles, BorderLayout.CENTER);
        heading.add(filters, BorderLayout.EAST);

        plotHolder.setOpaque(false);
        plot.setPreferredSize(new Dimension(580, 188));
        plotHolder.add(plot, BorderLayout.CENTER);
        summary.setFont(Theme.FONT_11);
        summary.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_1, 0, 0, 0));

        card.add(heading, BorderLayout.NORTH);
        card.add(plotHolder, BorderLayout.CENTER);
        card.add(summary, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    void refresh() {
        if (!workspace.hasScope()) {
            visibleSessions = List.of();
            buckets = List.of();
            showEmpty("Belum ada siswa terpilih",
                    "Pilih siswa pada bagian atas halaman untuk melihat ringkasan sesi belajarnya.");
            summary.setText("Durasi sesi tersimpan ditampilkan sebagai durasi terjadwal.");
            return;
        }

        try {
            List<Subject> subjects = workspace.learning().subjects(
                    workspace.session().token(), workspace.studentId());
            List<StudySession> sessions = workspace.learning().sessions(
                    workspace.session().token(), workspace.studentId());
            reloadSubjectFilter(subjects);

            SubjectOption selectedSubject = (SubjectOption) subjectFilter.getSelectedItem();
            PeriodOption selectedPeriod = (PeriodOption) periodFilter.getSelectedItem();
            LocalDate today = LocalDate.now();
            LocalDate start = today.minusDays((selectedPeriod == null ? 30 : selectedPeriod.days()) - 1L);
            List<StudySession> historicalSessions = sessions.stream()
                    .filter(value -> selectedSubject == null || selectedSubject.id() == null
                            || selectedSubject.id().equals(value.getSubjectId()))
                    .filter(value -> !value.getDate().isAfter(today))
                    .sorted(Comparator.comparing(StudySession::getDate)
                            .thenComparing(StudySession::getStartTime).thenComparing(StudySession::getId))
                    .toList();
            periodFilter.setEnabled(!historicalSessions.isEmpty());
            visibleSessions = historicalSessions.stream()
                    .filter(value -> !value.getDate().isBefore(start))
                    .toList();
            boolean containsDemo = visibleSessions.stream()
                    .anyMatch(value -> workspace.isDemo(LearningWorkspaceService.TYPE_SESSION, value.getId()));
            if (containsDemo) {
                boolean allDemo = visibleSessions.stream()
                        .allMatch(value -> workspace.isDemo(LearningWorkspaceService.TYPE_SESSION, value.getId()));
                plot.setNotice(allDemo ? "Data contoh akun demo" : "Termasuk sesi data demo");
            } else {
                plot.setNotice("");
            }

            buckets = aggregate(visibleSessions, selectedPeriod != null && selectedPeriod.weekly());
            if (visibleSessions.isEmpty()) {
                showEmpty("Belum ada sesi pada periode ini",
                        "Grafik hanya merangkum sesi bertanggal sampai hari ini; jadwal mendatang tidak ditampilkan.");
                summary.setText("Tidak ada durasi sesi tersimpan pada rentang tanggal yang dipilih.");
            } else {
                plot.setBuckets(buckets);
                plotHolder.removeAll();
                plotHolder.add(plot, BorderLayout.CENTER);
                long totalMinutes = visibleSessions.stream().mapToLong(StudySession::getDurationMinutes).sum();
                summary.setText(String.format(Locale.forLanguageTag("id"),
                        "%d menit terjadwal dalam %d sesi pada periode ini; tidak menunjukkan durasi aktual atau kehadiran.",
                        totalMinutes, visibleSessions.size()));
            }
        } catch (RuntimeException exception) {
            visibleSessions = List.of();
            buckets = List.of();
            showEmpty("Grafik sesi tidak dapat dimuat",
                    exception.getMessage() == null ? "Periksa koneksi data lalu muat ulang halaman." : exception.getMessage());
            summary.setText("Data sesi tidak tersedia.");
        }
        revalidate();
        repaint();
    }

    private void reloadSubjectFilter(List<Subject> subjects) {
        SubjectOption current = (SubjectOption) subjectFilter.getSelectedItem();
        updatingFilters = true;
        try {
            subjectFilter.removeAllItems();
            subjectFilter.addItem(new SubjectOption(null, "Semua mata pelajaran"));
            for (Subject subject : subjects) {
                subjectFilter.addItem(new SubjectOption(subject.getId(), subject.getName()));
            }
            if (current != null) {
                for (int index = 0; index < subjectFilter.getItemCount(); index++) {
                    if (current.id() != null && current.id().equals(subjectFilter.getItemAt(index).id())) {
                        subjectFilter.setSelectedIndex(index);
                        return;
                    }
                }
            }
            subjectFilter.setSelectedIndex(0);
        } finally {
            updatingFilters = false;
        }
    }

    private void showEmpty(String title, String detail) {
        plotHolder.removeAll();
        EmptyState empty = new EmptyState(title, detail);
        empty.setPreferredSize(new Dimension(580, 174));
        plotHolder.add(empty, BorderLayout.CENTER);
        plot.setBuckets(List.of());
        revalidate();
        repaint();
    }

    private static List<Bucket> aggregate(List<StudySession> sessions, boolean weekly) {
        Map<LocalDate, MutableBucket> totals = new TreeMap<>();
        for (StudySession session : sessions) {
            LocalDate key = weekly
                    ? session.getDate().with(DayOfWeek.MONDAY) : session.getDate();
            MutableBucket bucket = totals.computeIfAbsent(key, ignored -> new MutableBucket());
            bucket.minutes += session.getDurationMinutes();
            bucket.sessionCount++;
        }
        return totals.entrySet().stream()
                .map(entry -> new Bucket(entry.getKey(), entry.getValue().minutes, entry.getValue().sessionCount))
                .toList();
    }

    List<StudySession> visibleSessions() {
        return List.copyOf(visibleSessions);
    }

    List<Bucket> buckets() {
        return List.copyOf(buckets);
    }

    String summaryText() {
        return summary.getText();
    }

    private record SubjectOption(String id, String label) {
        @Override public String toString() { return label; }
    }

    private record PeriodOption(String label, int days, boolean weekly) {
        @Override public String toString() { return label; }
    }

    record Bucket(LocalDate startDate, long durationMinutes, int sessionCount) { }

    private static final class MutableBucket {
        long minutes;
        int sessionCount;
    }

    private static final class DurationPlot extends JComponent {
        private static final int LEFT = 54;
        private static final int RIGHT = 18;
        private static final int TOP = 18;
        private static final int BOTTOM = 34;
        private List<Bucket> buckets = List.of();
        private List<java.awt.Rectangle> barLocations = List.of();
        private String notice = "";

        DurationPlot() {
            setOpaque(false);
            setToolTipText("");
        }

        void setBuckets(List<Bucket> values) {
            buckets = List.copyOf(values);
            repaint();
        }

        void setNotice(String value) {
            notice = value;
            repaint();
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            for (int index = 0; index < barLocations.size(); index++) {
                if (barLocations.get(index).contains(event.getPoint())) {
                    Bucket bucket = buckets.get(index);
                    return String.format(Locale.forLanguageTag("id"),
                            "<html><b>%s</b><br>%d menit terjadwal<br>%d sesi%s</html>",
                            bucket.startDate().format(WEEK_LABEL), bucket.durationMinutes(),
                            bucket.sessionCount(), notice.isEmpty() ? "" : "<br>" + notice);
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(Theme.FONT_10);
            int width = getWidth();
            int height = getHeight();
            if (width <= LEFT + RIGHT || height <= TOP + BOTTOM) {
                g.dispose();
                return;
            }
            int chartWidth = width - LEFT - RIGHT;
            int chartHeight = height - TOP - BOTTOM;
            long maximum = buckets.stream().mapToLong(Bucket::durationMinutes).max().orElse(0);
            long step = maximum <= 60 ? 15 : maximum <= 240 ? 60 : (long) Math.ceil(maximum / 4.0 / 15.0) * 15;
            long axisMaximum = Math.max(step, (long) Math.ceil(maximum / (double) step) * step);
            FontMetrics metrics = g.getFontMetrics();
            for (int tick = 0; tick <= 4; tick++) {
                long value = axisMaximum * (4 - tick) / 4;
                int y = TOP + chartHeight * tick / 4;
                String label = value + " m";
                g.setColor(Theme.TEXT_MUTED);
                g.drawString(label, LEFT - metrics.stringWidth(label) - 8, y + metrics.getAscent() / 2);
                g.setColor(Theme.alpha(Theme.BORDER, 175));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(LEFT, y, width - RIGHT, y);
            }

            if (!buckets.isEmpty()) {
                int slotWidth = Math.max(1, chartWidth / buckets.size());
                int barWidth = Math.max(4, Math.min(28, slotWidth * 3 / 5));
                List<java.awt.Rectangle> locations = new ArrayList<>();
                for (int index = 0; index < buckets.size(); index++) {
                    Bucket bucket = buckets.get(index);
                    int center = LEFT + (int) Math.round(slotWidth * (index + 0.5));
                    int barHeight = (int) Math.round(chartHeight * bucket.durationMinutes() / axisMaximum);
                    int barY = TOP + chartHeight - barHeight;
                    java.awt.Rectangle bar = new java.awt.Rectangle(center - barWidth / 2, barY, barWidth,
                            Math.max(3, barHeight));
                    locations.add(bar);
                    g.setColor(Theme.ACCENT);
                    g.fillRoundRect(bar.x, bar.y, bar.width, bar.height, Theme.RADIUS_SM, Theme.RADIUS_SM);
                    if (buckets.size() <= 14) {
                        String value = bucket.durationMinutes() + "m";
                        g.setColor(Theme.TEXT);
                        int labelX = Math.max(LEFT, Math.min(width - RIGHT - metrics.stringWidth(value),
                                center - metrics.stringWidth(value) / 2));
                        int labelY = Math.max(TOP + metrics.getAscent(), barY - 5);
                        g.drawString(value, labelX, labelY);
                    }
                    if (buckets.size() <= 14 || index == 0 || index == buckets.size() - 1 || index % 5 == 0) {
                        String date = bucket.startDate().format(DAY_LABEL);
                        int labelX = Math.max(LEFT, Math.min(width - RIGHT - metrics.stringWidth(date),
                                center - metrics.stringWidth(date) / 2));
                        g.setColor(Theme.TEXT_MUTED);
                        g.drawString(date, labelX, height - 8);
                    }
                }
                barLocations = List.copyOf(locations);
            } else {
                barLocations = List.of();
            }
            if (!notice.isEmpty()) {
                g.setColor(Theme.WARNING);
                g.drawString(notice, LEFT, TOP - 3);
            }
            g.dispose();
        }
    }
}
