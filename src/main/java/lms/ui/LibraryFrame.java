package lms.ui;

import lms.Library;
import lms.model.Book;
import lms.model.BorrowRecord;
import lms.model.Member;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

public final class LibraryFrame extends JFrame {
    private static final Color NAVY = new Color(31, 58, 95);
    private static final Color TEAL = new Color(42, 157, 143);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color MUTED = new Color(104, 115, 130);
    private static final Color RED = new Color(190, 55, 55);

    private final Library library;
    private final JLabel totalBooks = new JLabel();
    private final JLabel availableBooks = new JLabel();
    private final JLabel borrowedBooks = new JLabel();
    private final JLabel totalMembers = new JLabel();
    private final JLabel status = new JLabel("Ready");
    private DefaultTableModel dashboardBorrowRows;

    public LibraryFrame(Library library) {
        super("Library Management System");
        this.library = library;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1080, 700);
        setLocationRelativeTo(null);
        buildInterface();
        refreshDashboard();
    }

    private void buildInterface() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(20, 22, 14, 22));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Library Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel("Manage books, members and borrowing");
        subtitle.setForeground(MUTED);
        JPanel titleStack = new JPanel(new GridLayout(0, 1, 0, 4));
        titleStack.setOpaque(false);
        titleStack.add(title);
        titleStack.add(subtitle);
        JButton exit = button("Exit", false);
        exit.addActionListener(event -> dispose());
        heading.add(titleStack, BorderLayout.WEST);
        heading.add(exit, BorderLayout.EAST);
        root.add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Books", booksPanel());
        tabs.addTab("Members", membersPanel());
        tabs.addTab("Borrow / Return", borrowingPanel());
        root.add(tabs, BorderLayout.CENTER);
        status.setForeground(MUTED);
        root.add(status, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel dashboardPanel() {
        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(BACKGROUND);
        page.setBorder(BorderFactory.createEmptyBorder(18, 4, 4, 4));

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        stats.add(statCard("Total books", totalBooks, NAVY));
        stats.add(statCard("Available", availableBooks, TEAL));
        stats.add(statCard("Borrowed", borrowedBooks, new Color(93, 120, 156)));
        stats.add(statCard("Members", totalMembers, NAVY));
        page.add(stats, BorderLayout.NORTH);

        JPanel current = card();
        current.setLayout(new BorderLayout(0, 12));
        current.add(sectionTitle("Currently borrowed books"), BorderLayout.NORTH);
        JTable table = table(new String[]{"Book", "Member", "Borrowed on"});
        dashboardBorrowRows = (DefaultTableModel) table.getModel();
        current.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(current, BorderLayout.CENTER);
        return page;
    }

    private JPanel booksPanel() {
        JPanel page = card();
        page.setLayout(new BorderLayout(0, 12));
        JTextField search = input();
        search.putClientProperty("JTextField.placeholderText", "Search by book ID, title, author or category");
        JButton add = button("Add book", true);
        JButton remove = button("Remove selected", false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(add);
        actions.add(remove);
        page.add(toolbar(search, actions), BorderLayout.NORTH);

        JTable table = table(new String[]{"Book ID", "Title", "Author", "Category", "Availability"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        Runnable refresh = () -> {
            DefaultTableModel rows = (DefaultTableModel) table.getModel();
            rows.setRowCount(0);
            for (Book book : library.searchBooks(search.getText())) {
                rows.addRow(new Object[]{book.getBookId(), book.getTitle(), book.getAuthor(),
                        book.getCategory(), book.getAvailabilityStatus()});
            }
            if (rows.getRowCount() == 0) status.setText("No books found.");
            refreshDashboard();
        };
        add.addActionListener(event -> addBookDialog(refresh));
        remove.addActionListener(event -> {
            String id = selectedValue(table, 0);
            if (id == null) return;
            if (JOptionPane.showConfirmDialog(this, "Remove book " + id + "?", "Confirm removal",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                perform(() -> library.removeBook(id), "Book removed.", refresh);
            }
        });
        search.getDocument().addDocumentListener(onChange(refresh));
        refresh.run();
        return page;
    }

    private JPanel membersPanel() {
        JPanel page = card();
        page.setLayout(new BorderLayout(0, 12));
        JTextField search = input();
        search.putClientProperty("JTextField.placeholderText", "Search by member ID, name or contact number");
        JButton add = button("Add member", true);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(add);
        page.add(toolbar(search, actions), BorderLayout.NORTH);

        JTable table = table(new String[]{"Member ID", "Member name", "Contact number"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        Runnable refresh = () -> {
            DefaultTableModel rows = (DefaultTableModel) table.getModel();
            rows.setRowCount(0);
            for (Member member : library.searchMembers(search.getText())) {
                rows.addRow(new Object[]{member.getMemberId(), member.getName(), member.getContactNumber()});
            }
            if (rows.getRowCount() == 0) status.setText("No members found.");
            refreshDashboard();
        };
        add.addActionListener(event -> addMemberDialog(refresh));
        search.getDocument().addDocumentListener(onChange(refresh));
        refresh.run();
        return page;
    }

    private JPanel borrowingPanel() {
        JPanel page = card();
        page.setLayout(new BorderLayout(0, 12));

        JComboBox<MemberChoice> members = new JComboBox<>();
        JComboBox<BookChoice> books = new JComboBox<>();
        JButton borrow = button("Borrow book", true);
        JPanel form = new JPanel(new GridLayout(1, 5, 10, 0));
        form.setOpaque(false);
        form.add(new JLabel("Member"));
        form.add(members);
        form.add(new JLabel("Available book"));
        form.add(books);
        form.add(borrow);
        page.add(form, BorderLayout.NORTH);

        JTable table = table(new String[]{"Book ID", "Book", "Member", "Borrowed on"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton returnBook = button("Return selected book", false);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(returnBook);
        page.add(footer, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            members.removeAllItems();
            library.getMembers().stream()
                    .sorted(java.util.Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER))
                    .forEach(member -> members.addItem(new MemberChoice(member)));
            books.removeAllItems();
            library.getBooks().stream().filter(Book::isAvailable)
                    .sorted(java.util.Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                    .forEach(book -> books.addItem(new BookChoice(book)));
            DefaultTableModel rows = (DefaultTableModel) table.getModel();
            rows.setRowCount(0);
            for (BorrowRecord record : library.getActiveBorrowRecords()) {
                rows.addRow(new Object[]{record.getBook().getBookId(), record.getBook().getTitle(),
                        record.getMember().getName(), record.getBorrowDate()});
            }
            refreshDashboard();
        };
        borrow.addActionListener(event -> {
            MemberChoice member = (MemberChoice) members.getSelectedItem();
            BookChoice book = (BookChoice) books.getSelectedItem();
            if (member == null || book == null) {
                showMessage("Add a member and an available book before borrowing.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            perform(() -> library.borrowBook(member.member().getMemberId(), book.book().getBookId()),
                    "Book borrowed successfully.", refresh);
        });
        returnBook.addActionListener(event -> {
            String bookId = selectedValue(table, 0);
            if (bookId != null) {
                perform(() -> library.returnBookByBookId(bookId), "Book returned successfully.", refresh);
            }
        });
        refresh.run();
        return page;
    }

    private void addBookDialog(Runnable refresh) {
        JTextField id = input();
        JTextField title = input();
        JTextField author = input();
        JTextField category = input();
        JPanel form = form(new String[]{"Book ID", "Book title", "Author", "Category"},
                new Component[]{id, title, author, category});
        showFormDialog("Add book", form, () -> library.addBook(
                new Book(id.getText(), title.getText(), author.getText(), category.getText())),
                "Book added successfully.", refresh);
    }

    private void addMemberDialog(Runnable refresh) {
        JTextField id = input();
        JTextField name = input();
        JTextField contact = input();
        JPanel form = form(new String[]{"Member ID", "Member name", "Contact number"},
                new Component[]{id, name, contact});
        showFormDialog("Add member", form, () -> library.addMember(
                new Member(id.getText(), name.getText(), contact.getText())),
                "Member added successfully.", refresh);
    }

    private void showFormDialog(String title, JPanel form, Runnable saveAction,
                                String successMessage, Runnable refresh) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        body.add(form, BorderLayout.CENTER);
        JLabel error = new JLabel(" ");
        error.setForeground(RED);
        body.add(error, BorderLayout.SOUTH);

        JButton save = button("Save", true);
        JButton cancel = button("Cancel", false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setBackground(Color.WHITE);
        actions.add(cancel);
        actions.add(save);
        JPanel content = new JPanel(new BorderLayout());
        content.add(body, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(save);
        cancel.addActionListener(event -> dialog.dispose());
        save.addActionListener(event -> {
            try {
                saveAction.run();
                dialog.dispose();
                status.setText(successMessage);
                refresh.run();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                error.setText(ex.getMessage());
            }
        });
        dialog.pack();
        dialog.setMinimumSize(new Dimension(460, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void perform(Runnable action, String successMessage, Runnable refresh) {
        try {
            action.run();
            status.setText(successMessage);
            refresh.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showMessage(ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshDashboard() {
        totalBooks.setText(Integer.toString(library.getBooks().size()));
        availableBooks.setText(Long.toString(library.getAvailableBookCount()));
        borrowedBooks.setText(Long.toString(library.getBorrowedBookCount()));
        totalMembers.setText(Integer.toString(library.getMembers().size()));
        if (dashboardBorrowRows != null) {
            dashboardBorrowRows.setRowCount(0);
            for (BorrowRecord record : library.getActiveBorrowRecords()) {
                dashboardBorrowRows.addRow(new Object[]{record.getBook().getTitle(),
                        record.getMember().getName(), record.getBorrowDate()});
            }
        }
    }

    private String selectedValue(JTable table, int column) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a row first.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return table.getValueAt(table.convertRowIndexToModel(row), column).toString();
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "Library Management System", type);
    }

    private static JPanel toolbar(JTextField search, JPanel actions) {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        toolbar.add(search, BorderLayout.CENTER);
        toolbar.add(actions, BorderLayout.EAST);
        return toolbar;
    }

    private static JPanel form(String[] labels, Component[] fields) {
        JPanel panel = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            panel.add(new JLabel(labels[i]));
            panel.add(fields[i]);
        }
        return panel;
    }

    private static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 233, 239)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    private static JPanel statCard(String label, JLabel value, Color accent) {
        JPanel panel = card();
        panel.setLayout(new GridLayout(2, 1, 0, 6));
        value.setFont(new Font("Segoe UI", Font.BOLD, 27));
        value.setForeground(accent);
        JLabel caption = new JLabel(label);
        caption.setForeground(MUTED);
        panel.add(value);
        panel.add(caption);
        return panel;
    }

    private static JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(NAVY);
        return label;
    }

    private static JTable table(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(29);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(232, 236, 241));
        table.getTableHeader().setBackground(new Color(239, 243, 248));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return table;
    }

    private static JTextField input() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(240, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(216, 223, 231)),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        return field;
    }

    private static JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setBackground(primary ? TEAL : new Color(232, 238, 245));
        button.setForeground(primary ? Color.WHITE : NAVY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        return button;
    }

    private static DocumentListener onChange(Runnable action) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { action.run(); }
            @Override public void removeUpdate(DocumentEvent event) { action.run(); }
            @Override public void changedUpdate(DocumentEvent event) { action.run(); }
        };
    }

    private record MemberChoice(Member member) {
        @Override
        public String toString() {
            return member.getName() + " (" + member.getMemberId() + ")";
        }
    }

    private record BookChoice(Book book) {
        @Override
        public String toString() {
            return book.getTitle() + " (" + book.getBookId() + ")";
        }
    }
}
