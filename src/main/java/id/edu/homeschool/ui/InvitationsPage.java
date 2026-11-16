package id.edu.homeschool.ui;

import id.edu.homeschool.model.InvitationStatus;
import id.edu.homeschool.service.CreatedInvitation;
import id.edu.homeschool.service.InvitationSummary;
import id.edu.homeschool.ui.design.DataTable;
import id.edu.homeschool.ui.design.Dialogs;
import id.edu.homeschool.ui.design.PageHeader;
import id.edu.homeschool.ui.design.Theme;
import id.edu.homeschool.ui.design.Ui;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Teacher page listing the invitation codes that can be shared with students. */
public final class InvitationsPage extends JPanel implements AppShell.Refreshable {

    private final Workspace workspace;
    private final PageHeader header = new PageHeader("Undangan",
            "Kode undangan berlaku tujuh hari. Kode hanya ditampilkan sekali dan hanya hash-nya yang disimpan.");
    private final DataTable<InvitationSummary> table;

    public InvitationsPage(Workspace workspace) {
        super(new BorderLayout(0, Theme.SPACE_4));
        this.workspace = workspace;
        setBackground(Theme.CANVAS);
        setOpaque(false);

        table = new DataTable<>(new String[]{"Dibuat", "Berlaku sampai", "Status", "Permintaan menunggu"},
                () -> workspace.connections().ownInvitations(workspace.session().token()),
                invitation -> new Object[]{TeacherRequestsPage.format(invitation.createdAt()),
                        TeacherRequestsPage.format(invitation.expiresAt()), statusLabel(invitation.status()),
                        invitation.pendingRequests()});
        table.setEmptyState("Belum ada kode undangan",
                "Buat kode undangan lalu bagikan kepada siswa yang ingin terhubung.");
        table.setColumnRenderer(2, DataTable.statusRenderer());
        table.addFilter("Status", List.of("Aktif", "Sudah dipakai", "Dicabut", "Kedaluwarsa"),
                invitation -> statusLabel(invitation.status()));

        JButton create = Ui.button("Buat kode undangan", Ui.Variant.PRIMARY);
        JButton revoke = Ui.button("Cabut kode", Ui.Variant.DANGER);
        create.addActionListener(event -> createCode());
        revoke.addActionListener(event -> revokeSelected());
        table.addAction(create);
        table.addAction(revoke);

        add(header, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
        workspace.onChange(this::refresh);
    }

    private void createCode() {
        try {
            CreatedInvitation invitation = workspace.connections().createInvitation(workspace.session().token());
            Dialogs.code(this, "Kode undangan dibuat",
                    "Bagikan kode ini kepada siswa. Kode hanya ditampilkan sekali, simpan salinannya sekarang.",
                    invitation.code());
            table.refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Kode undangan tidak dapat dibuat", exception.getMessage());
        }
    }

    private void revokeSelected() {
        InvitationSummary selected = table.selected();
        if (selected == null) {
            Dialogs.info(this, "Belum ada pilihan", "Pilih satu kode undangan pada tabel sebelum mencabutnya.");
            return;
        }
        if (!Dialogs.confirm(this, "Konfirmasi pencabutan",
                "Cabut kode undangan ini? Siswa tidak akan dapat memakainya untuk membuat permintaan koneksi.")) return;
        try {
            workspace.connections().revokeInvitation(workspace.session().token(), selected.id());
            Dialogs.success(this, "Kode dicabut",
                    "Kode undangan ini sudah tidak berlaku dan tidak dapat dipakai lagi.");
            table.refresh();
        } catch (RuntimeException exception) {
            Dialogs.error(this, "Kode undangan tidak dapat dicabut", exception.getMessage());
        }
    }

    public void refresh() {
        table.refresh();
    }

    static String statusLabel(InvitationStatus status) {
        return switch (status) {
            case ACTIVE -> "Aktif";
            case USED -> "Sudah dipakai";
            case REVOKED -> "Dicabut";
            case EXPIRED -> "Kedaluwarsa";
        };
    }
}
