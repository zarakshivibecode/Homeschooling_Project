package id.edu.homeschool.ui;

import id.edu.homeschool.model.Evaluation;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A lightweight Swing line chart based only on persisted, date-stamped evaluations. */
final class EvaluationTrendChart extends JPanel {
    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id"));
    private static final DateTimeFormatter DATE_TOOLTIP = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id"));

    private final Workspace workspace;
    private final JComboBox<SubjectOption> subjectFilter = Ui.combo();
    private final JComboBox<PeriodOption> periodFilter = Ui.combo();
    private final JPanel plotHolder = new JPanel(new BorderLayout());
    private final JLabel trendSummary = Ui.muted("Tren terlihat setelah tersedia minimal dua evaluasi.");
    private final TrendPlot plot = new TrendPlot();
    private List<Evaluation> visibleEvaluations = List.of();
    private boolean updatingFilters;

    EvaluationTrendChart(Workspace workspace) {
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
        JLabel title = Ui.label("Perkembangan nilai");
        title.setFont(Theme.FONT_14_SEMIBOLD);
        JLabel subtitle = Ui.subtle("Nilai (%) per tanggal.");
        subtitle.setFont(Theme.FONT_11);
        titles.add(title);
        titles.add(subtitle);

        subjectFilter.setPreferredSize(new Dimension(190, Theme.CONTROL_HEIGHT));
        subjectFilter.addActionListener(event -> {
            if (!updatingFilters) refresh();
        });
        periodFilter.setPreferredSize(new Dimension(135, Theme.CONTROL_HEIGHT));
        periodFilter.addItem(new PeriodOption("Semua waktu", 0));
        periodFilter.addItem(new PeriodOption("30 hari", 30));
        periodFilter.addItem(new PeriodOption("90 hari", 90));
        periodFilter.addItem(new PeriodOption("1 tahun", 365));
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
        trendSummary.setFont(Theme.FONT_11);
        trendSummary.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_1, 0, 0, 0));

        card.add(heading, BorderLayout.NORTH);
        card.add(plotHolder, BorderLayout.CENTER);
        card.add(trendSummary, BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);
        refresh();
    }

    void refresh() {
        if (!workspace.hasScope()) {
            visibleEvaluations = List.of();
            showEmpty("Belum ada siswa terpilih",
                    "Pilih siswa pada bagian atas halaman untuk melihat perkembangan nilainya.");
            trendSummary.setText("Tren terlihat setelah tersedia minimal dua evaluasi.");
            return;
        }

        try {
            List<Subject> subjects = workspace.learning().subjects(
                    workspace.session().token(), workspace.studentId());
            List<Evaluation> evaluations = workspace.learning().evaluations(
                    workspace.session().token(), workspace.studentId());
            reloadSubjectFilter(subjects);

            SubjectOption selectedSubject = (SubjectOption) subjectFilter.getSelectedItem();
            List<Evaluation> subjectEvaluations = evaluations.stream()
                    .filter(value -> selectedSubject == null || selectedSubject.id() == null
                            || selectedSubject.id().equals(value.getSubjectId()))
                    .toList();
            boolean hasMultipleDates = subjectEvaluations.stream().map(Evaluation::getDate)
                    .distinct().limit(2).count() > 1;
            if (!hasMultipleDates && periodFilter.getSelectedIndex() != 0) {
                updatingFilters = true;
                try {
                    periodFilter.setSelectedIndex(0);
                } finally {
                    updatingFilters = false;
                }
            }
            periodFilter.setEnabled(hasMultipleDates);
            PeriodOption selectedPeriod = (PeriodOption) periodFilter.getSelectedItem();
            LocalDate today = LocalDate.now();
            LocalDate cutoff = selectedPeriod == null || selectedPeriod.days() == 0
                    ? null : today.minusDays(selectedPeriod.days());

            visibleEvaluations = subjectEvaluations.stream()
                    .filter(value -> cutoff == null || !value.getDate().isBefore(cutoff))
                    .sorted(Comparator.comparing(Evaluation::getDate).thenComparing(Evaluation::getId))
                    .toList();

            boolean containsDemo = visibleEvaluations.stream()
                    .anyMatch(value -> workspace.isDemo(LearningWorkspaceService.TYPE_EVALUATION, value.getId()));
            if (containsDemo) {
                boolean allDemo = visibleEvaluations.stream()
                        .allMatch(value -> workspace.isDemo(LearningWorkspaceService.TYPE_EVALUATION, value.getId()));
                plot.setNotice(allDemo ? "Data contoh akun demo" : "Termasuk evaluasi data demo");
            } else {
                plot.setNotice("");
            }

            if (visibleEvaluations.isEmpty()) {
                showEmpty("Belum ada evaluasi pada filter ini",
                        "Grafik akan menampilkan nilai setelah evaluasi tersimpan pada mata pelajaran dan periode yang dipilih.");
                trendSummary.setText("Tren terlihat setelah tersedia minimal dua evaluasi.");
            } else {
                plot.setEvaluations(visibleEvaluations);
                plotHolder.removeAll();
                plotHolder.add(plot, BorderLayout.CENTER);
                trendSummary.setText(buildTrendSummary(visibleEvaluations));
            }
        } catch (RuntimeException exception) {
            visibleEvaluations = List.of();
            showEmpty("Grafik nilai tidak dapat dimuat",
                    exception.getMessage() == null ? "Periksa koneksi data lalu muat ulang halaman." : exception.getMessage());
            trendSummary.setText("Data evaluasi tidak tersedia.");
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
        plot.setEvaluations(List.of());
        revalidate();
        repaint();
    }

    private static String buildTrendSummary(List<Evaluation> values) {
        if (values.size() < 2) {
            return "Satu evaluasi tersimpan. Tren naik atau turun belum dapat disimpulkan.";
        }
        Evaluation first = values.getFirst();
        Evaluation last = values.getLast();
        double delta = last.getPercentage() - first.getPercentage();
        String movement = Math.abs(delta) < 0.05 ? "relatif stabil"
                : delta > 0 ? "meningkat" : "menurun";
        String change = String.format(Locale.ROOT, "%+.1f poin persentase", delta);
        return "Dari " + first.getDate() + " ke " + last.getDate() + ", nilai "
                + movement + " (" + change + "). Perbandingan memakai evaluasi pertama dan terakhir yang tampil.";
    }

    List<Evaluation> visibleEvaluations() {
        return List.copyOf(visibleEvaluations);
    }

    String trendSummaryText() {
        return trendSummary.getText();
    }

    private record SubjectOption(String id, String label) {
        @Override public String toString() { return label; }
    }

    private record PeriodOption(String label, int days) {
        @Override public String toString() { return label; }
    }

    private static final class TrendPlot extends JComponent {
        private static final int LEFT = 48;
        private static final int RIGHT = 18;
        private static final int TOP = 18;
        private static final int BOTTOM = 34;
        private List<Evaluation> evaluations = List.of();
        private List<java.awt.Point> markerLocations = List.of();
        private String notice = "";

        TrendPlot() {
            setOpaque(false);
            setToolTipText("");
        }

        void setEvaluations(List<Evaluation> values) {
            evaluations = List.copyOf(values);
            repaint();
        }

        void setNotice(String value) {
            notice = value;
            repaint();
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            for (int index = 0; index < markerLocations.size(); index++) {
                java.awt.Point point = markerLocations.get(index);
                if (point.distance(event.getPoint()) <= 12) {
                    Evaluation evaluation = evaluations.get(index);
                    return "<html><b>" + evaluation.getDate().format(DATE_TOOLTIP) + "</b><br>"
                            + String.format(Locale.ROOT, "Nilai: %.1f / %.1f (%.1f%%)",
                                    evaluation.getScore(), evaluation.getMaximumScore(), evaluation.getPercentage())
                            + (notice.isEmpty() ? "" : "<br>" + notice) + "</html>";
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
            g.setColor(Theme.TEXT_MUTED);
            FontMetrics metrics = g.getFontMetrics();
            for (int tick : new int[]{100, 50, 0}) {
                int y = TOP + (int) Math.round(chartHeight * (100 - tick) / 100.0);
                String label = tick + "%";
                g.drawString(label, LEFT - metrics.stringWidth(label) - 8, y + metrics.getAscent() / 2);
                g.setColor(Theme.alpha(Theme.BORDER, 175));
                g.setStroke(new BasicStroke(1f));
                g.drawLine(LEFT, y, width - RIGHT, y);
                g.setColor(Theme.TEXT_MUTED);
            }

            if (!evaluations.isEmpty()) {
                List<java.awt.Point> points = new ArrayList<>();
                for (int index = 0; index < evaluations.size(); index++) {
                    int x = evaluations.size() == 1 ? LEFT + chartWidth / 2
                            : LEFT + (int) Math.round(chartWidth * index / (double) (evaluations.size() - 1));
                    double percentage = evaluations.get(index).getPercentage();
                    int y = TOP + (int) Math.round(chartHeight * (100 - percentage) / 100.0);
                    points.add(new java.awt.Point(x, y));
                }
                markerLocations = List.copyOf(points);
                g.setColor(Theme.ACCENT);
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int index = 1; index < points.size(); index++) {
                    java.awt.Point previous = points.get(index - 1);
                    java.awt.Point current = points.get(index);
                    g.drawLine(previous.x, previous.y, current.x, current.y);
                }
                for (int index = 0; index < points.size(); index++) {
                    java.awt.Point point = points.get(index);
                    g.setColor(Theme.GLASS_SOLID);
                    g.fillOval(point.x - 5, point.y - 5, 10, 10);
                    g.setColor(Theme.ACCENT_DEEP);
                    g.setStroke(new BasicStroke(2f));
                    g.drawOval(point.x - 5, point.y - 5, 10, 10);
                    if (evaluations.size() <= 12) {
                        g.setColor(Theme.TEXT);
                        String value = String.format(Locale.ROOT, "%.0f%%", evaluations.get(index).getPercentage());
                        int labelX = Math.max(LEFT, Math.min(width - RIGHT - metrics.stringWidth(value),
                                point.x - metrics.stringWidth(value) / 2));
                        int labelY = point.y < TOP + 22 ? point.y + 18 : point.y - 9;
                        g.drawString(value, labelX, labelY);
                    }
                }
                Evaluation first = evaluations.getFirst();
                Evaluation last = evaluations.getLast();
                String start = first.getDate().format(DATE_LABEL);
                String end = last.getDate().format(DATE_LABEL);
                g.setColor(Theme.TEXT_MUTED);
                if (evaluations.size() == 1) {
                    g.drawString(start, Math.max(LEFT, points.getFirst().x - metrics.stringWidth(start) / 2),
                            height - 8);
                } else {
                    g.drawString(start, LEFT, height - 8);
                    g.drawString(end, width - RIGHT - metrics.stringWidth(end), height - 8);
                }
            } else {
                markerLocations = List.of();
            }

            if (!notice.isEmpty()) {
                g.setColor(Theme.WARNING);
                g.drawString(notice, LEFT, TOP - 3);
            }
            g.dispose();
        }
    }
}
