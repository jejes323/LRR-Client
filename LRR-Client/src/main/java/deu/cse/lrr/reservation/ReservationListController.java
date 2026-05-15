package deu.cse.lrr.reservation;

import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;

public class ReservationListController {
    private final ReservationListView view;
    private final String userId;
    private final Socket socket;

    public ReservationListController(ReservationListView view, String userId, Socket socket) {
        this.view = view;
        this.userId = userId;
        this.socket = socket;
        
        setupTableRenderer();
        initController();
        loadReservationList();
    }

    private void setupTableRenderer() {
        JTable table = getTable("jTable1");
        if (table == null) return;

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int col) {
                Component c = super.getTableCellRendererComponent(t, v, isS, hasF, r, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // 테이블에 데이터가 있을 때만 색상 처리
                if (r < t.getRowCount() && t.getColumnCount() >= 5) {
                    Object statusValue = t.getValueAt(r, 4);
                    String status = (statusValue != null) ? statusValue.toString() : "";

                    if (isS) {
                        c.setBackground(t.getSelectionBackground());
                    } else {
                        switch (status) {
                            case "승인 완료" -> c.setBackground(new Color(204, 255, 204));
                            case "승인 대기" -> c.setBackground(new Color(255, 255, 204));
                            case "승인 거부" -> c.setBackground(new Color(255, 204, 204));
                            default -> c.setBackground(Color.WHITE);
                        }
                    }
                }
                return c;
            }
        });
    }

    private void initController() {
        JButton btnRefresh = getButton("jButton2");
        JButton btnCancel = getButton("jButton1");
        JButton btnBack = getButton("jButton3");

        if (btnRefresh != null) btnRefresh.addActionListener(e -> loadReservationList());
        if (btnBack != null) btnBack.addActionListener(e -> view.dispose());
        if (btnCancel != null) {
            btnCancel.addActionListener(e -> {
                JOptionPane.showMessageDialog(view, "취소 기능은 다음 단계에서 구현될 예정입니다.");
            });
        }
    }

    private void loadReservationList() {
        JButton btnRefresh = getButton("jButton2");
        if (btnRefresh != null) btnRefresh.setEnabled(false);

        new Thread(() -> {
            try {
                if (socket == null) {
                    SwingUtilities.invokeLater(() -> {
                        updateTable("911호|2026-05-20|11:00 - 13:00|테스트용|승인 완료");
                        if (btnRefresh != null) btnRefresh.setEnabled(true);
                    });
                    return;
                }

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.write("GET_RESERVATION_LIST:" + userId + "\n");
                out.flush();

                String response = in.readLine();
                System.out.println("서버 응답: " + response); // 디버깅용 로그

                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.startsWith("RESERVATION_LIST:")) {
                        updateTable(response.substring(17));
                    } else if ("NO_RESERVATIONS".equals(response)) {
                        updateTable("");
                        JOptionPane.showMessageDialog(view, "예약 내역이 없습니다.");
                    }
                    if (btnRefresh != null) btnRefresh.setEnabled(true);
                });
            } catch (IOException e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    if (btnRefresh != null) btnRefresh.setEnabled(true);
                });
            }
        }).start();
    }

    private void updateTable(String dataStr) {
        JTable table = getTable("jTable1");
        if (table == null) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        if (dataStr == null || dataStr.isEmpty()) return;

        String[] reservations = dataStr.split(";");
        for (String res : reservations) {
            String[] details = res.split("\\|");
            if (details.length >= 5) {
                model.addRow(new Object[]{details[0], details[1], details[2], details[3], details[4]});
            }
        }
    }

    // 리플렉션 헬퍼
    private JButton getButton(String name) { return (JButton) getField(name); }
    private JTable getTable(String name) { return (JTable) getField(name); }

    private Object getField(String name) {
        try {
            Field f = view.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(view);
        } catch (Exception e) {
            return null;
        }
    }
}
