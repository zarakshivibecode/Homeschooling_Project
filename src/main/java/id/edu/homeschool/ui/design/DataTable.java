package id.edu.homeschool.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

/**
 * A reusable list view: search field, filters, action bar, sortable table and an informative
 * empty state. The page supplies the loader and the row mapper; everything else is shared
 * behaviour so every screen looks and behaves the same.
 */
public final class DataTable<T> extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField search = Ui.textField();
    private final JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, Theme.SPACE_2, 0));
    private final JPanel filters = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Theme.SPACE_2, 0));
    private final JPanel holder = new JPanel(new BorderLayout(0, Theme.SPACE_2));
    private final GlassPanel contentCard = new GlassPanel(new BorderLayout());
    private final JLabel hint = Ui.subtle("");
    private final Supplier<List<T>> loader;
    private final Function<T, Object[]> mapper;
    private final Map<String, Filter> filterMap = new LinkedHashMap<>();
    private List<T> items = List.of();
    private List<T> visible = List.of();
    private String loadError;
    private String emptyTitle = "Belum ada data";
    private String emptyDescription = "Gunakan tombol tambah untuk membuat data pertama.";

    /** One optional column filter rendered as a dropdown next to the search field. */
    private static final class Filter {
        private final String caption;
        private final java.util.function.Supplier<List<String>> optionsSupplier;
        private final Function<Object, String> extractor;
        private final JComboBox<String> combo = Ui.combo();

        Filter(String caption, java.util.function.Supplier<List<String>> optionsSupplier,
                Function<Object, String> extractor) {
            this.caption = caption;
            this.optionsSupplier = optionsSupplier;
            this.extractor = extractor;
            combo.setPreferredSize(new Dimension(170, Theme.CONTROL_HEIGHT));
            reload();
        }

        /** Rebuilds the option list while keeping the current selection when it still exists. */
        void reload() {
            Object previous = combo.getSelectedItem();
            combo.removeAllItems();
            combo.addItem("Semua " + caption.toLowerCase(Locale.ROOT));
            try {
                List<String> options = optionsSupplier.get();
                if (options != null) options.forEach(combo::addItem);
            } catch (RuntimeException ignored) {
                // The options are not available yet, for example while no student is selected.
            }
            if (previous != null) {
                for (int index = 0; index < combo.getItemCount(); index++) {
                    if (combo.getItemAt(index).equals(previous)) {
                        combo.setSelectedIndex(index);
                        return;
                    }
                }
            }
        }

        String prefix() { return "Semua " + caption.toLowerCase(Locale.ROOT); }
    }

    public DataTable(String[] columns, Supplier<List<T>> loader, Function<T, Object[]> mapper) {
        super(new BorderLayout(0, Theme.SPACE_3));
        this.loader = loader;
        this.mapper = mapper;

        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Ui.styleTable(table);

        setBackground(Theme.CANVAS);
        setOpaque(false);

        actions.setBackground(Theme.CANVAS);
        actions.setOpaque(false);
        filters.setBackground(Theme.CANVAS);
        filters.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(Theme.SPACE_3, 0));
        toolbar.setBackground(Theme.CANVAS);
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Theme.SPACE_2, 0));
        searchBox.setBackground(Theme.CANVAS);
        searchBox.setOpaque(false);
        JLabel searchLabel = Ui.subtle("Cari");
        search.setPreferredSize(new Dimension(230, Theme.CONTROL_HEIGHT));
        search.setToolTipText("Cari pada tabel ini");
        searchBox.add(searchLabel);
        searchBox.add(search);
        filters.add(searchBox);

        toolbar.add(filters, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);

        contentCard.setFill(Theme.GLASS);
        contentCard.setRadius(Theme.RADIUS);
        contentCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        contentCard.add(Ui.scroll(table), BorderLayout.CENTER);

        holder.setBackground(Theme.CANVAS);
        holder.setOpaque(false);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent event) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent event) { applyFilter(); }
        });

        add(toolbar, BorderLayout.NORTH);
        add(holder, BorderLayout.CENTER);
        refresh();
    }

    public DataTable<T> addAction(JComponent component) {
        actions.add(component);
        return this;
    }

    /**
     * Adds a dropdown filter for a column. The filter combines with the free text search so
     * both can be used at the same time. The options are reloaded on every refresh so the filter
     * always matches the data that is currently visible.
     */
    public DataTable<T> addFilter(String caption, java.util.function.Supplier<List<String>> options,
            Function<T, String> extractor) {
        Filter filter = new Filter(caption, options, item -> extractor.apply((T) item));
        filterMap.put(caption, filter);
        filters.add(filter.combo);
        filter.combo.addActionListener(event -> applyFilter());
        return this;
    }

    /** Adds a dropdown filter with a fixed option list. */
    public DataTable<T> addFilter(String caption, List<String> options, Function<T, String> extractor) {
        return addFilter(caption, () -> options, extractor);
    }

    public DataTable<T> setEmptyState(String title, String description) {
        this.emptyTitle = title;
        this.emptyDescription = description;
        return this;
    }

    public DataTable<T> setColumnRenderer(int column, TableCellRenderer renderer) {
        if (column < table.getColumnCount()) table.getColumnModel().getColumn(column).setCellRenderer(renderer);
        return this;
    }

    public DataTable<T> setHint(String text) {
        hint.setText(text == null ? "" : text);
        hint.setVisible(!hint.getText().isEmpty());
        return this;
    }

    public JTable table() { return table; }

    /** Applies a search term programmatically; used by the interface tests. */
    public void search(String term) {
        search.setText(term == null ? "" : term);
    }

    /**
     * Reloads the rows from the loader and reapplies the current search term and filters.
     * A failing loader never opens a modal window: the problem is shown inside the table area
     * so the page stays usable and the event thread is never blocked.
     */
    public void refresh() {
        loadError = null;
        try {
            items = new ArrayList<>(loader.get());
        } catch (RuntimeException exception) {
            items = List.of();
            loadError = exception.getMessage() == null
                    ? "Periksa penyimpanan atau file JSON." : exception.getMessage();
        }
        for (Filter filter : filterMap.values()) filter.reload();
        applyFilter();
    }

    private void applyFilter() {
        String term = search.getText().trim().toLowerCase(Locale.ROOT);
        Map<String, String> activeFilters = new LinkedHashMap<>();
        for (Filter filter : filterMap.values()) {
            Object selected = filter.combo.getSelectedItem();
            if (selected == null) continue;
            String value = selected.toString();
            if (!value.equals(filter.prefix())) activeFilters.put(filter.caption, value);
        }
        visible = new ArrayList<>();
        model.setRowCount(0);
        for (T item : items) {
            Object[] values;
            try {
                values = mapper.apply(item);
            } catch (RuntimeException exception) {
                // One unreadable row must not break the whole table.
                values = new Object[]{"—", exception.getMessage() == null ? "" : exception.getMessage()};
            }
            if (!term.isEmpty() && !matches(values, term)) continue;
            if (!passesFilters(item, activeFilters)) continue;
            visible.add(item);
            model.addRow(values);
        }
        updateEmptyState(term);
    }

    private boolean passesFilters(T item, Map<String, String> activeFilters) {
        for (Map.Entry<String, String> entry : activeFilters.entrySet()) {
            Filter filter = filterMap.get(entry.getKey());
            if (filter == null) continue;
            String value = filter.extractor.apply(item);
            if (!entry.getValue().equals(value == null ? "" : value)) return false;
        }
        return true;
    }

    private static boolean matches(Object[] values, String term) {
        for (Object value : values) {
            if (value != null && value.toString().toLowerCase(Locale.ROOT).contains(term)) return true;
        }
        return false;
    }

    private void updateEmptyState(String term) {
        holder.removeAll();
        if (loadError != null) {
            holder.add(new EmptyState("Data tidak dapat dimuat", loadError), BorderLayout.CENTER);
        } else if (items.isEmpty()) {
            holder.add(new EmptyState(emptyTitle, emptyDescription), BorderLayout.CENTER);
        } else if (visible.isEmpty()) {
            holder.add(new EmptyState("Tidak ada hasil yang cocok",
                    "Ubah kata pencarian \"" + term + "\" atau muat ulang data."), BorderLayout.CENTER);
        } else {
            holder.add(contentCard, BorderLayout.CENTER);
        }
        holder.add(hint, BorderLayout.SOUTH);
        holder.revalidate();
        holder.repaint();
    }

    /** The currently selected row mapped back to its source object, or {@code null}. */
    public T selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= visible.size()) return null;
        return visible.get(modelRow);
    }

    /** The rows currently visible after the search filter, in display order. */
    public List<T> visibleItems() {
        return List.copyOf(visible);
    }

    public List<T> allItems() { return List.copyOf(items); }

    /** Renders a status cell as a small coloured pill. */
    public static TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            private final JPanel cell = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
            private final StatusBadge badge = new StatusBadge("", Theme.neutral());

            {
                cell.setOpaque(true);
                cell.add(badge);
            }

            @Override public Component getTableCellRendererComponent(JTable owner, Object value, boolean selected,
                    boolean focused, int row, int column) {
                String text = value == null ? "—" : value.toString();
                Theme.StatusStyle style = Theme.statusOf(text);
                badge.set(text, style);
                cell.setBackground(selected ? owner.getSelectionBackground()
                        : (row % 2 == 0 ? Theme.GLASS_TABLE : Theme.GLASS_ROW_ALT));
                return cell;
            }
        };
    }
}
