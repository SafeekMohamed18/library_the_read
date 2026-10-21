package lms.ui;

import lms.model.Book;
import lms.model.Librarian;
import lms.model.Rental;
import lms.model.Member;
import lms.service.LibraryService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class LmsFrame extends JFrame {
    private static final Color NAVY = new Color(31, 58, 95);
    private static final Color TEAL = new Color(42, 157, 143);
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color TEXT = new Color(34, 34, 34);
    private static final Color MUTED = new Color(104, 115, 130);
    private static final Color RED = new Color(214, 69, 69);

    private final LibraryService service;
    private Librarian signedInUser;
    private String currentPage = "Dashboard";
    private JPanel content;
    private JLabel pageHeading;

    public LmsFrame(LibraryService service) {
        super("The Read | Library Management System");
        this.service = service;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND);
        if (service.needsLibrarianSetup()) {
            showSetup();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        JPanel page = authLayout("Welcome back", "Sign in to manage your library");
        JTextField username = textField();
        JPasswordField password = new JPasswordField();
        styleInput(password);
        JButton login = primaryButton("Sign in");
        JLabel error = new JLabel(" ");
        error.setForeground(RED);
        JPanel form = formPanel();
        addFormRow(form, "Username", username);
        addFormRow(form, "Password", password);
        form.add(login);
        form.add(error);
        ((JPanel) page.getClientProperty("authCenter")).add(form, BorderLayout.CENTER);
        login.addActionListener(event -> {
            char[] secret = password.getPassword();
            try {
                Librarian user = service.authenticate(username.getText(), secret);
                if (user == null) {
                    error.setText("The username or password is incorrect.");
                    password.setText("");
                    return;
                }
                signedInUser = user;
                showWorkspace();
            } finally {
                java.util.Arrays.fill(secret, '\0');
            }
        });
        password.addActionListener(event -> login.doClick());
        setContentPane(page);
        revalidate();
        repaint();
    }

    private void showSetup() {
        JPanel page = authLayout("Set up your library", "Create the first librarian account");
        JTextField username = textField();
        JTextField fullName = textField();
        JTextField phone = textField();
        JTextField email = textField();
        JPasswordField password = new JPasswordField();
        styleInput(password);
        JLabel error = new JLabel(" ");
        error.setForeground(RED);
        JPanel form = formPanel();
        addFormRow(form, "Username", username);
        addFormRow(form, "Full name", fullName);
        addFormRow(form, "Phone (10 digits)", phone);
        addFormRow(form, "Email", email);
        addFormRow(form, "Password (8+ characters)", password);
        JButton create = primaryButton("Create account");
        form.add(create);
        form.add(error);
        ((JPanel) page.getClientProperty("authCenter")).add(form, BorderLayout.CENTER);
        create.addActionListener(event -> {
            char[] secret = password.getPassword();
            try {
                service.createFirstLibrarian(username.getText(), fullName.getText(), phone.getText(),
                        email.getText(), secret);
                showLogin();
                JOptionPane.showMessageDialog(this, "Librarian account created. Please sign in.",
                        "Setup complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                error.setText(ex.getMessage());
            } catch (IOException ex) {
                showError(ex);
            } finally {
                java.util.Arrays.fill(secret, '\0');
                password.setText("");
            }
        });
        setContentPane(page);
        revalidate();
        repaint();
    }

    private JPanel authLayout(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        JPanel brand = new JPanel();
        brand.setBackground(NAVY);
        brand.setPreferredSize(new Dimension(320, 1));
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(90, 36, 30, 30));
        JLabel logo = new JLabel("THE READ");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logo.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("<html>A calmer way to<br>manage your library.</html>");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tagline.setForeground(new Color(219, 231, 242));
        tagline.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        brand.add(logo);
        brand.add(tagline);
        page.add(brand, BorderLayout.WEST);
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(50, 70, 50, 70));
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 27));
        heading.setForeground(TEXT);
        JLabel helper = new JLabel(subtitle);
        helper.setForeground(MUTED);
        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.add(heading);
        intro.add(Box.createVerticalStrut(8));
        intro.add(helper);
        center.add(intro, BorderLayout.NORTH);
        page.add(center, BorderLayout.CENTER);
        page.putClientProperty("authCenter", center);
        return page;
    }

    private JPanel formPanel() {
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 10));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(35, 0, 0, 0));
        return form;
    }

    private void showWorkspace() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.add(sidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND);
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(16, 26, 16, 26));
        pageHeading = new JLabel(currentPage);
        pageHeading.setFont(new Font("Segoe UI", Font.BOLD, 21));
        pageHeading.setForeground(TEXT);
        JLabel user = new JLabel(signedInUser.getName() + "   ·   Librarian");
        user.setForeground(MUTED);
        top.add(pageHeading, BorderLayout.WEST);
        top.add(user, BorderLayout.EAST);
        main.add(top, BorderLayout.NORTH);
        content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(22, 26, 26, 26));
        main.add(content, BorderLayout.CENTER);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        navigate("Dashboard");
        revalidate();
        repaint();
    }

    private JPanel sidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(215, 1));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 14, 18, 14));
        JLabel logo = new JLabel("THE READ");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 21));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 12, 28, 0));
        sidebar.add(logo);
        String[] pages = {"Dashboard", "Books", "Members", "Issue / Return", "Reports"};
        for (String page : pages) {
            JButton button = new JButton(page);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setForeground(Color.WHITE);
            button.setBackground(NAVY);
            button.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 8));
            button.setFocusPainted(false);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            button.addActionListener(event -> navigate(page));
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(5));
        }
        sidebar.add(Box.createVerticalGlue());
        JButton logout = new JButton("Log out");
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setForeground(new Color(236, 216, 216));
        logout.setBackground(NAVY);
        logout.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 8));
        logout.setFocusPainted(false);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        logout.addActionListener(event -> {
            signedInUser = null;
            showLogin();
        });
        sidebar.add(logout);
        return sidebar;
    }

    private void navigate(String page) {
        currentPage = page;
        pageHeading.setText(page);
        content.removeAll();
        switch (page) {
            case "Dashboard" -> content.add(dashboardPage(), BorderLayout.CENTER);
            case "Books" -> content.add(booksPage(), BorderLayout.CENTER);
            case "Members" -> content.add(membersPage(), BorderLayout.CENTER);
            case "Issue / Return" -> content.add(rentalsPage(), BorderLayout.CENTER);
            case "Reports" -> content.add(reportsPage(), BorderLayout.CENTER);
            default -> throw new IllegalArgumentException("Unknown page: " + page);
        }
        content.revalidate();
        content.repaint();
    }

    private JPanel dashboardPage() {
        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setOpaque(false);
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("Book copies", Long.toString(service.getTotalBookCopies()), NAVY));
        stats.add(statCard("Active members", Long.toString(service.getActiveMemberCount()), TEAL));
        stats.add(statCard("Books borrowed", Long.toString(service.getIssuedCount()), new Color(93, 120, 156)));
        stats.add(statCard("Overdue", Long.toString(service.getOverdueCount()), RED));
        page.add(stats, BorderLayout.NORTH);
        JPanel duePanel = panelCard();
        duePanel.setLayout(new BorderLayout(0, 12));
        duePanel.add(sectionTitle("Due and overdue rentals"), BorderLayout.NORTH);
        JTable table = table(new String[]{"Member", "Book", "Issue date", "Due date", "Status"});
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (Rental rental : service.getActiveRentals()) {
            Member member = service.findMember(rental.getMemberId());
            Book book = service.findBook(rental.getIsbn());
            String status = rental.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Borrowed";
            model.addRow(new Object[]{member.getName(), book.getTitle(), rental.getIssueDate(),
                    rental.getDueDate(), status});
        }
        duePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        page.add(duePanel, BorderLayout.CENTER);
        return page;
    }

    private JPanel booksPage() {
        JPanel page = panelCard();
        page.setLayout(new BorderLayout(0, 14));
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JTextField search = textField();
        search.putClientProperty("JTextField.placeholderText", "Search title, author, ISBN or category");
        toolbar.add(search, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = primaryButton("Add book");
        JButton edit = secondaryButton("Edit");
        JButton delete = dangerButton("Delete");
        actions.add(add);
        actions.add(edit);
        actions.add(delete);
        toolbar.add(actions, BorderLayout.EAST);
        page.add(toolbar, BorderLayout.NORTH);

        JTable table = table(new String[]{"ISBN", "Title", "Author", "Category", "Available", "Total"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        Runnable refresh = () -> {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            for (Book book : service.searchBooks(search.getText())) {
                model.addRow(new Object[]{book.getIsbn(), book.getTitle(), book.getAuthor(), book.getCategory(),
                        book.getAvailableCopies(), book.getTotalCopies()});
            }
        };
        add.addActionListener(event -> bookDialog(null, refresh));
        edit.addActionListener(event -> {
            Book selected = selectedBook(table);
            if (selected != null) bookDialog(selected, refresh);
        });
        delete.addActionListener(event -> {
            Book selected = selectedBook(table);
            if (selected == null) return;
            if (JOptionPane.showConfirmDialog(this, "Delete \"" + selected.getTitle() + "\"?",
                    "Confirm deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                runAction(() -> service.deleteBook(selected.getIsbn()), "Book deleted.", refresh);
            }
        });
        search.getDocument().addDocumentListener(listener(refresh));
        refresh.run();
        return page;
    }

    private void bookDialog(Book existing, Runnable refresh) {
        JTextField isbn = textField();
        JTextField title = textField();
        JTextField author = textField();
        JTextField category = textField();
        JTextField publisher = textField();
        JTextField year = textField();
        JTextField price = textField();
        JTextField copies = textField();
        if (existing != null) {
            isbn.setText(existing.getIsbn());
            isbn.setEditable(false);
            title.setText(existing.getTitle());
            author.setText(existing.getAuthor());
            category.setText(existing.getCategory());
            publisher.setText(existing.getPublisher());
            year.setText(Integer.toString(existing.getYear()));
            price.setText(Double.toString(existing.getPrice()));
            copies.setText(Integer.toString(existing.getTotalCopies()));
        }
        JPanel form = dialogForm(new String[]{"ISBN (10 or 13 digits)", "Title", "Author", "Category",
                "Publisher", "Year", "Price (Rs.)", "Total copies"},
                new Component[]{isbn, title, author, category, publisher, year, price, copies});
        showFormDialog(existing == null ? "Add book" : "Edit book", form, () -> {
            int total = Integer.parseInt(copies.getText().trim());
            Book updated = new Book(isbn.getText(), title.getText(), author.getText(), category.getText(),
                    publisher.getText(), Integer.parseInt(year.getText().trim()),
                    Double.parseDouble(price.getText().trim()), total,
                    existing == null ? total : existing.getAvailableCopies());
            if (existing == null) service.addBook(updated);
            else service.updateBook(existing.getIsbn(), updated);
        }, existing == null ? "Book added." : "Book updated.", refresh);
    }

    private JPanel membersPage() {
        JPanel page = panelCard();
        page.setLayout(new BorderLayout(0, 14));
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JTextField search = textField();
        search.putClientProperty("JTextField.placeholderText", "Search member ID, name or phone");
        toolbar.add(search, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = primaryButton("Add member");
        JButton edit = secondaryButton("Edit");
        JButton status = secondaryButton("Deactivate / activate");
        JButton history = secondaryButton("History");
        actions.add(add);
        actions.add(edit);
        actions.add(status);
        actions.add(history);
        toolbar.add(actions, BorderLayout.EAST);
        page.add(toolbar, BorderLayout.NORTH);
        JTable table = table(new String[]{"Member ID", "Name", "Type", "Phone", "Email", "Status", "Books held"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        Runnable refresh = () -> {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            for (Member member : service.searchMembers(search.getText())) {
                long held = service.getActiveRentals().stream()
                        .filter(rental -> rental.getMemberId().equalsIgnoreCase(member.getId())).count();
                model.addRow(new Object[]{member.getId(), member.getName(), member.getMemberType(),
                        member.getPhone(), member.getEmail(), member.isActive() ? "Active" : "Inactive", held});
            }
        };
        add.addActionListener(event -> memberDialog(null, refresh));
        edit.addActionListener(event -> {
            Member selected = selectedMember(table);
            if (selected != null) memberDialog(selected, refresh);
        });
        status.addActionListener(event -> {
            Member selected = selectedMember(table);
            if (selected == null) return;
            boolean active = !selected.isActive();
            runAction(() -> service.setMemberActive(selected.getId(), active),
                    active ? "Member activated." : "Member deactivated.", refresh);
        });
        history.addActionListener(event -> {
            Member selected = selectedMember(table);
            if (selected == null) return;
            StringBuilder text = new StringBuilder("Rental history for ").append(selected.getName()).append(":\n\n");
            List<Rental> historyRows = service.getMemberHistory(selected.getId());
            if (historyRows.isEmpty()) text.append("No rentals recorded.");
            for (Rental rental : historyRows) {
                text.append(service.findBook(rental.getIsbn()).getTitle()).append("  ·  ")
                        .append(rental.getIssueDate()).append(" to ")
                        .append(rental.getReturnDate() == null ? "still borrowed" : rental.getReturnDate())
                        .append("  ·  Fine Rs. ").append(String.format("%.2f", rental.getFineAmount())).append('\n');
            }
            JOptionPane.showMessageDialog(this, text.toString(), "Member history", JOptionPane.INFORMATION_MESSAGE);
        });
        search.getDocument().addDocumentListener(listener(refresh));
        refresh.run();
        return page;
    }

    private void memberDialog(Member existing, Runnable refresh) {
        JTextField id = textField();
        JTextField name = textField();
        JTextField phone = textField();
        JTextField email = textField();
        JTextField address = textField();
        JComboBox<String> type = new JComboBox<>(new String[]{"Student", "Staff"});
        JTextField joinDate = textField();
        joinDate.setToolTipText("Date format: yyyy-MM-dd");
        if (existing != null) {
            id.setText(existing.getId());
            id.setEditable(false);
            name.setText(existing.getName());
            phone.setText(existing.getPhone());
            email.setText(existing.getEmail());
            address.setText(existing.getAddress());
            type.setSelectedItem(existing.getMemberType());
            joinDate.setText(existing.getJoinDate().toString());
        } else {
            joinDate.setText(LocalDate.now().toString());
        }
        JPanel form = dialogForm(new String[]{"Member ID", "Name", "Phone (10 digits)", "Email",
                "Address", "Member type", "Join date (yyyy-MM-dd)"},
                new Component[]{id, name, phone, email, address, type, joinDate});
        showFormDialog(existing == null ? "Add member" : "Edit member", form, () -> {
            Member updated = new Member(id.getText(), name.getText(), phone.getText(), email.getText(),
                    address.getText(), (String) type.getSelectedItem(), LocalDate.parse(joinDate.getText().trim()),
                    existing == null || existing.isActive());
            if (existing == null) service.addMember(updated);
            else service.updateMember(existing.getId(), updated);
        }, existing == null ? "Member added." : "Member updated.", refresh);
    }

    private JPanel rentalsPage() {
        JPanel page = panelCard();
        page.setLayout(new BorderLayout(0, 14));
        JPanel issueBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        issueBar.setOpaque(false);
        JTextField memberId = textField();
        JTextField isbn = textField();
        memberId.setPreferredSize(new Dimension(150, 36));
        isbn.setPreferredSize(new Dimension(190, 36));
        JButton issue = primaryButton("Issue book");
        issueBar.add(new JLabel("Member ID"));
        issueBar.add(memberId);
        issueBar.add(new JLabel("Book ISBN"));
        issueBar.add(isbn);
        issueBar.add(issue);
        page.add(issueBar, BorderLayout.NORTH);
        JTable table = table(new String[]{"Rental ID", "Member", "Book", "Issue date", "Due date",
                "Returned", "Fine (Rs.)", "Fine status", "Status"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton returnBook = primaryButton("Return selected");
        JButton renew = secondaryButton("Renew selected");
        JButton payFine = secondaryButton("Record fine payment");
        actions.add(returnBook);
        actions.add(renew);
        actions.add(payFine);
        page.add(actions, BorderLayout.SOUTH);
        Runnable refresh = () -> {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            for (Rental rental : service.getRentals()) {
                Book book = service.findBook(rental.getIsbn());
                Member member = service.findMember(rental.getMemberId());
                String status = rental.isActive()
                        ? (rental.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Borrowed") : "Returned";
                model.addRow(new Object[]{rental.getRentalId().substring(0, 8), member.getName(), book.getTitle(),
                        rental.getIssueDate(), rental.getDueDate(), rental.getReturnDate() == null ? "—" : rental.getReturnDate(),
                        String.format("%.2f", rental.getFineAmount()), rental.isFinePaid() ? "Paid" : "Unpaid", status});
            }
            table.putClientProperty("rentalRows", service.getRentals());
        };
        issue.addActionListener(event -> runAction(() -> service.issueBook(memberId.getText(), isbn.getText()),
                "Book issued. Due date: " + LocalDate.now().plusDays(LibraryService.RENTAL_DAYS), refresh));
        returnBook.addActionListener(event -> {
            Rental selected = selectedRental(table);
            if (selected == null || !selected.isActive()) {
                showMessage("Select an active rental to return.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long daysLate = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(selected.getDueDate(), LocalDate.now()));
            double fineEstimate = Math.min(daysLate * LibraryService.FINE_PER_DAY,
                    service.findBook(selected.getIsbn()).getPrice());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Return this book?\nFine due: Rs. " + String.format("%.2f", fineEstimate),
                    "Confirm return", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                runAction(() -> service.returnBook(selected.getRentalId()),
                        "Book returned. Fine: Rs. " + String.format("%.2f", fineEstimate), refresh);
            }
        });
        renew.addActionListener(event -> {
            Rental selected = selectedRental(table);
            if (selected != null) runAction(() -> service.renewRental(selected.getRentalId()),
                    "Rental renewed for 7 days.", refresh);
        });
        payFine.addActionListener(event -> {
            Rental selected = selectedRental(table);
            if (selected != null) runAction(() -> service.payFine(selected.getRentalId()),
                    "Fine payment recorded.", refresh);
        });
        refresh.run();
        return page;
    }

    @SuppressWarnings("unchecked")
    private Rental selectedRental(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a rental first.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        List<Rental> rows = (List<Rental>) table.getClientProperty("rentalRows");
        return rows.get(table.convertRowIndexToModel(row));
    }

    private JPanel reportsPage() {
        JPanel page = panelCard();
        page.setLayout(new BorderLayout(0, 14));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        JComboBox<String> reportType = new JComboBox<>(new String[]{"Overdue rentals", "Active rentals", "Member history"});
        JTextField memberId = textField();
        memberId.setPreferredSize(new Dimension(160, 36));
        memberId.setToolTipText("Member ID (used for member history)");
        JButton generate = primaryButton("Generate");
        JButton export = secondaryButton("Export CSV");
        toolbar.add(reportType);
        toolbar.add(memberId);
        toolbar.add(generate);
        toolbar.add(export);
        page.add(toolbar, BorderLayout.NORTH);
        JTable table = table(new String[]{"Member", "Book", "Issue date", "Due date", "Return date", "Fine (Rs.)", "Status"});
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        List<String[]> exportRows = new ArrayList<>();
        Runnable refresh = () -> {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);
            exportRows.clear();
            List<Rental> selection = switch ((String) reportType.getSelectedItem()) {
                case "Overdue rentals" -> service.getOverdueRentals();
                case "Active rentals" -> service.getActiveRentals();
                default -> service.getMemberHistory(memberId.getText());
            };
            for (Rental rental : selection) {
                Member member = service.findMember(rental.getMemberId());
                Book book = service.findBook(rental.getIsbn());
                String returned = rental.getReturnDate() == null ? "" : rental.getReturnDate().toString();
                String status = rental.isActive()
                        ? (rental.getDueDate().isBefore(LocalDate.now()) ? "Overdue" : "Borrowed") : "Returned";
                String fine = String.format("%.2f", rental.getFineAmount());
                model.addRow(new Object[]{member.getName(), book.getTitle(), rental.getIssueDate(),
                        rental.getDueDate(), returned.isEmpty() ? "—" : returned, fine, status});
                exportRows.add(new String[]{member.getName(), book.getTitle(), rental.getIssueDate().toString(),
                        rental.getDueDate().toString(), returned, fine, status});
            }
        };
        generate.addActionListener(event -> refresh.run());
        reportType.addActionListener(event -> memberId.setEnabled("Member history".equals(reportType.getSelectedItem())));
        memberId.setEnabled(false);
        export.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("library-report.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                runAction(() -> {
                    StringBuilder csv = new StringBuilder("Member,Book,Issue date,Due date,Return date,Fine,Status\n");
                    for (String[] row : exportRows) {
                        for (int i = 0; i < row.length; i++) {
                            if (i > 0) csv.append(',');
                            csv.append(csvEscape(row[i]));
                        }
                        csv.append('\n');
                    }
                    Files.writeString(chooser.getSelectedFile().toPath(), csv, StandardCharsets.UTF_8);
                }, "Report exported.", null);
            }
        });
        refresh.run();
        return page;
    }

    private Book selectedBook(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a book first.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return service.findBook((String) table.getValueAt(table.convertRowIndexToModel(row), 0));
    }

    private Member selectedMember(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showMessage("Select a member first.", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return service.findMember((String) table.getValueAt(table.convertRowIndexToModel(row), 0));
    }

    private void runAction(IoAction action, String successMessage, Runnable refresh) {
        try {
            action.run();
            if (successMessage != null) showMessage(successMessage, JOptionPane.INFORMATION_MESSAGE);
            if (refresh != null) refresh.run();
        } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not complete action",
                JOptionPane.ERROR_MESSAGE);
    }

    private void showFormDialog(String title, JPanel form, IoAction saveAction, String successMessage,
                                Runnable refresh) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));
        body.add(form, BorderLayout.CENTER);

        JLabel validationMessage = new JLabel(" ");
        validationMessage.setForeground(RED);
        body.add(validationMessage, BorderLayout.SOUTH);

        JButton save = primaryButton("Save");
        JButton cancel = secondaryButton("Cancel");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(Color.WHITE);
        actions.add(cancel);
        actions.add(save);

        JPanel dialogContent = new JPanel(new BorderLayout());
        dialogContent.add(body, BorderLayout.CENTER);
        dialogContent.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(dialogContent);
        dialog.getRootPane().setDefaultButton(save);
        cancel.addActionListener(event -> dialog.dispose());
        save.addActionListener(event -> {
            try {
                saveAction.run();
                dialog.dispose();
                showMessage(successMessage, JOptionPane.INFORMATION_MESSAGE);
                refresh.run();
            } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
                validationMessage.setText(ex.getMessage());
            }
        });
        dialog.pack();
        dialog.setMinimumSize(new Dimension(540, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showMessage(String message, int messageType) {
        JOptionPane.showMessageDialog(this, message, "The Read", messageType);
    }

    private static JPanel dialogForm(String[] labels, Component[] fields) {
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            form.add(fields[i]);
        }
        return form;
    }

    private static void addFormRow(JPanel form, String label, Component field) {
        form.add(new JLabel(label));
        form.add(field);
    }

    private static JTable table(String[] headers) {
        DefaultTableModel model = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(232, 236, 241));
        table.getTableHeader().setBackground(new Color(239, 243, 248));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return table;
    }

    private static JTextField textField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    private static void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(260, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(216, 223, 231)),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(TEAL);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(232, 238, 245));
        button.setForeground(NAVY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    private static JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(RED);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        return button;
    }

    private static JPanel panelCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 233, 239)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    private static JLabel sectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(NAVY);
        return title;
    }

    private static JPanel statCard(String label, String value, Color accent) {
        JPanel card = panelCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel number = new JLabel(value);
        number.setFont(new Font("Segoe UI", Font.BOLD, 28));
        number.setForeground(accent);
        JLabel caption = new JLabel(label);
        caption.setForeground(MUTED);
        card.add(number);
        card.add(Box.createVerticalStrut(7));
        card.add(caption);
        return card;
    }

    private static DocumentListener listener(Runnable change) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { change.run(); }
            @Override public void removeUpdate(DocumentEvent event) { change.run(); }
            @Override public void changedUpdate(DocumentEvent event) { change.run(); }
        };
    }

    private static String csvEscape(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0
                && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) return value;
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @FunctionalInterface
    private interface IoAction {
        void run() throws IOException;
    }
}
