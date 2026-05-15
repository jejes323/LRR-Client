package deu.cse.lrr.dashboard;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * 통합 사용자 대시보드 컨트롤러
 * 학생 및 교수 공용으로 사용되며, 역할에 따라 UI를 동적으로 제어합니다.
 */
public class UserDashboardController {
    
    private final UserDashboardView view;
    private LocalDate currentDate;
    
    public UserDashboardController(UserDashboardView view, String userName, String role) {
        this.view = view;
        this.currentDate = LocalDate.now();
        
        initUIComponents();
        initController();
        updateWelcomeMessage(userName, role);
        updateRoomComboItems(); // 초기 상태(일별 보기)에 맞춰 UI 숨김/표시 설정
        updateDashboard();
    }
    
    private void updateWelcomeMessage(String userName, String role) {
        JLabel welcomeLabel = getLabel("welcomeLabel");
        if (welcomeLabel != null) {
            String roleSuffix = "";
            if ("PROFESSOR".equals(role)) roleSuffix = " 교수님";
            else if ("STUDENT".equals(role)) roleSuffix = " 학생님";
            else if ("ASSISTANT".equals(role)) roleSuffix = " 조교님";
            
            welcomeLabel.setText(userName + roleSuffix + " 환영합니다.");
        }
    }
    
    private void initUIComponents() {
        JComboBox<String> viewModeCombo = getComboBox("viewModeCombo");
        JComboBox<String> roomSelectCombo = getComboBox("roomSelectCombo");
        
        if (viewModeCombo != null) {
            viewModeCombo.setModel(new DefaultComboBoxModel<>(new String[]{"일별 보기", "주별 보기", "월별 보기"}));
        }
        if (roomSelectCombo != null) {
            roomSelectCombo.setModel(new DefaultComboBoxModel<>(new String[]{"전체 강의실", "911호", "915호", "916호", "918호(실습실)"}));
        }
    }

    private void initController() {
        JButton btnPrev = getButton("btnPrev");
        JButton btnNext = getButton("btnNext");
        JComboBox<String> viewModeCombo = getComboBox("viewModeCombo");
        JComboBox<String> roomSelectCombo = getComboBox("roomSelectCombo");

        if (btnPrev != null) btnPrev.addActionListener(e -> moveDate(-1));
        if (btnNext != null) btnNext.addActionListener(e -> moveDate(1));
        
        if (viewModeCombo != null) {
            viewModeCombo.addActionListener(e -> {
                updateRoomComboItems();
                updateDashboard();
            });
        }
        
        if (roomSelectCombo != null) {
            roomSelectCombo.addActionListener(e -> updateDashboard());
        }
        
        JButton btnReserve = getButton("btnReserve");
        if (btnReserve != null) {
            btnReserve.addActionListener(e -> JOptionPane.showMessageDialog(view, "강의실 예약 모달 창이 열립니다."));
        }
        
        JButton btnMyReservation = getButton("btnMyReservation");
        if (btnMyReservation != null) {
            btnMyReservation.addActionListener(e -> JOptionPane.showMessageDialog(view, "내 예약 조회/취소 모달 창이 열립니다."));
        }
        
        JButton btnLogout = getButton("btnLogout");
        if (btnLogout != null) {
            btnLogout.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(view, "로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) view.dispose();
            });
        }
    }
    
    private void moveDate(int offset) {
        JComboBox<String> viewModeCombo = getComboBox("viewModeCombo");
        if (viewModeCombo == null) return;

        int viewMode = viewModeCombo.getSelectedIndex();
        if (viewMode == 0) currentDate = currentDate.plusDays(offset);
        else if (viewMode == 1) currentDate = currentDate.plusWeeks(offset);
        else if (viewMode == 2) currentDate = currentDate.plusMonths(offset);
        
        updateDashboard();
    }
    
    private void updateDashboard() {
        JComboBox<String> viewModeCombo = getComboBox("viewModeCombo");
        JComboBox<String> roomSelectCombo = getComboBox("roomSelectCombo");
        JLabel dateLabel = getLabel("dateLabel");
        
        if (viewModeCombo == null || roomSelectCombo == null || dateLabel == null) return;

        int viewMode = viewModeCombo.getSelectedIndex();
        String selectedRoom = (String) roomSelectCombo.getSelectedItem();
        
        DateTimeFormatter formatter;
        if (viewMode == 0) {
            formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 (E)", Locale.KOREAN);
            dateLabel.setText(currentDate.format(formatter) + " 일별 현황");
        } else if (viewMode == 1) {
            LocalDate startOfWeek = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(4);
            formatter = DateTimeFormatter.ofPattern("MM/dd", Locale.KOREAN);
            dateLabel.setText("이번 주 (" + startOfWeek.format(formatter) + " ~ " + endOfWeek.format(formatter) + ") " + selectedRoom + " 현황");
        } else if (viewMode == 2) {
            dateLabel.setText(currentDate.getYear() + "년 " + currentDate.getMonthValue() + "월 " + selectedRoom + " 현황");
        }
        
        setupTimetableUI(viewMode, selectedRoom);
    }

    private void updateRoomComboItems() {
        JComboBox<String> viewModeCombo = getComboBox("viewModeCombo");
        JComboBox<String> roomSelectCombo = getComboBox("roomSelectCombo");
        JLabel lblRoomSelect = getLabel("lblRoomSelect");
        
        if (viewModeCombo == null || roomSelectCombo == null) return;

        int viewMode = viewModeCombo.getSelectedIndex();
        String currentSelection = (String) roomSelectCombo.getSelectedItem();
        
        // 리스너 임시 제거 (무한 루프 방지)
        ActionListener[] listeners = roomSelectCombo.getActionListeners();
        for (ActionListener l : listeners) roomSelectCombo.removeActionListener(l);

        if (viewMode >= 1) { // 주별 또는 월별 보기
            if (lblRoomSelect != null) lblRoomSelect.setVisible(true);
            roomSelectCombo.setVisible(true);
            roomSelectCombo.setEnabled(true);
            
            // 주별/월별 보기일 때는 강의실 목록을 보여줌
            roomSelectCombo.removeAllItems();
            roomSelectCombo.addItem("911호");
            roomSelectCombo.addItem("915호");
            roomSelectCombo.addItem("916호");
            roomSelectCombo.addItem("918호(실습실)");
            
            if (currentSelection != null && !currentSelection.equals("전체 강의실")) {
                roomSelectCombo.setSelectedItem(currentSelection);
            } else {
                roomSelectCombo.setSelectedIndex(0);
            }
        } else { // 일별 보기
            // 일별 보기일 때는 모든 강의실을 한꺼번에 보여주므로 강의실 선택 UI를 숨김
            if (lblRoomSelect != null) lblRoomSelect.setVisible(false);
            roomSelectCombo.setVisible(false);
            
            roomSelectCombo.removeAllItems();
            roomSelectCombo.addItem("전체 강의실");
            roomSelectCombo.setSelectedIndex(0);
        }

        // 리스너 복구
        for (ActionListener l : listeners) roomSelectCombo.addActionListener(l);
    }
    
    private void setupTimetableUI(int viewMode, String selectedRoom) {
        try {
            java.lang.reflect.Field field = view.getClass().getDeclaredField("roomStatusScrollPane");
            field.setAccessible(true);
            javax.swing.JScrollPane scrollPane = (javax.swing.JScrollPane) field.get(view);
            
            String[] columns;
            String[][] data;
            
            if (viewMode == 0) {
                columns = new String[]{"시간", "911호", "915호", "916호", "918호(실습실)"};
                data = new String[][]{
                    {"09:00 - 10:00", "", "", "", "예약불가"},
                    {"10:00 - 11:00", "", "강의중", "", "예약불가"},
                    {"11:00 - 12:00", "예약완료", "강의중", "", ""},
                    {"12:00 - 13:00", "예약완료", "", "", ""},
                    {"13:00 - 14:00", "", "", "보강", ""},
                    {"14:00 - 15:00", "", "", "보강", ""},
                    {"15:00 - 16:00", "", "", "", ""},
                    {"16:00 - 17:00", "승인대기", "", "", ""},
                };
            } else if (viewMode == 1) {
                columns = new String[]{"시간", "월", "화", "수", "목", "금"};
                data = new String[][]{
                    {"09:00 - 10:00", "강의중", "", "강의중", "", ""},
                    {"10:00 - 11:00", "강의중", "", "강의중", "", ""},
                    {"11:00 - 12:00", "", "예약완료", "", "", "예약불가"},
                    {"12:00 - 13:00", "", "예약완료", "", "보강", "예약불가"},
                    {"13:00 - 14:00", "예약대기", "", "", "보강", ""},
                    {"14:00 - 15:00", "", "", "", "", ""},
                    {"15:00 - 16:00", "", "", "강의중", "", ""},
                    {"16:00 - 17:00", "", "", "강의중", "", ""},
                };
            } else { // viewMode == 2 (월별 보기)
                int daysInMonth = currentDate.lengthOfMonth();
                columns = new String[]{"날짜", "09시", "10시", "11시", "12시", "13시", "14시", "15시", "16시"};
                data = new String[daysInMonth][9];
                
                for (int i = 0; i < daysInMonth; i++) {
                    LocalDate date = currentDate.withDayOfMonth(i + 1);
                    data[i][0] = (i + 1) + "일 (" + date.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN)) + ")";
                    
                    for (int j = 1; j < 9; j++) {
                        if (date.getDayOfWeek().getValue() >= 6) {
                            data[i][j] = "예약불가"; // 주말
                        } else {
                            // 더미 데이터 생성 로직 (실제 데이터 연동 시 교체 필요)
                            if ((i + j) % 15 == 0) data[i][j] = "강의중";
                            else if ((i + j) % 19 == 0) data[i][j] = "예약완료";
                            else data[i][j] = "";
                        }
                    }
                }
            }
            
            DefaultTableModel model = new DefaultTableModel(data, columns) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            
            JTable table = new JTable(model);
            table.setRowHeight(40);
            table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int col) {
                    Component c = super.getTableCellRendererComponent(t, v, isS, hasF, r, col);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (col == 0) {
                        c.setBackground(java.awt.Color.WHITE); // 시간 열 배경색 제거 (흰색)
                        c.setForeground(java.awt.Color.BLACK);
                    } else {
                        String s = v != null ? v.toString() : "";
                        if (s.isEmpty()) c.setBackground(Color.WHITE);
                        else if (s.equals("예약불가") || s.equals("강의중") || s.equals("보강")) {
                            c.setBackground(new Color(255, 204, 204)); c.setForeground(new Color(200, 0, 0));
                        } else if (s.equals("예약완료")) {
                            c.setBackground(new Color(204, 229, 255)); c.setForeground(new Color(0, 0, 200));
                        } else if (s.equals("승인대기") || s.equals("예약대기")) {
                            c.setBackground(new Color(255, 255, 204)); c.setForeground(new Color(180, 150, 0));
                        }
                    }
                    return c;
                }
            });
            scrollPane.setViewportView(table);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    // 리플렉션 헬퍼 메서드들
    private JButton getButton(String name) { return (JButton) getField(name); }
    private JComboBox<String> getComboBox(String name) { return (JComboBox<String>) getField(name); }
    private JLabel getLabel(String name) { return (JLabel) getField(name); }
    
    private Object getField(String name) {
        try {
            java.lang.reflect.Field f = view.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(view);
        } catch (Exception e) { return null; }
    }
}
