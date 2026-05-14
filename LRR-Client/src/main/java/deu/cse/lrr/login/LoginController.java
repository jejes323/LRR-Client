package deu.cse.lrr.login;

import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {
    private LoginView view;
    private LoginModel model;

    public LoginController(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;

        this.view.addLoginListener(new LoginListener());
        this.view.addRegisterListener(new RegisterListener());
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = view.getId();
            String password = view.getPassword();
            String role = view.getSelectedRole();

            if (role.isEmpty()) {
                view.showMessage("역할을 선택하지 않았습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String response = model.authenticate(id, password, role);

            if (response == null) {
                view.showMessage("서버 응답이 없습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("LOGIN_SUCCESS".equals(response)) {
                view.showMessage(id + "님 (" + role + ") 로그인 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
                // 로그인 성공 시 연결 유지, 다음 창 호출 등의 로직 추가
                // TODO: 이후 메인 화면(다음 창)을 띄울 때 model.getSocket() 등을 전달하세요.
            } else if ("ALREADY_LOGGED_IN".equals(response)) {
                view.showMessage("이미 접속 중인 아이디입니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                closeSocket();
            } else if (response.startsWith("ERROR:")) {
                view.showMessage("서버 연결 실패: " + response.substring(6), "오류", JOptionPane.ERROR_MESSAGE);
                closeSocket();
            } else {
                view.showMessage("아이디 또는 비밀번호가 일치하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                closeSocket();
            }
        }
    }

    class RegisterListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Register().setVisible(true);
        }
    }

    private void closeSocket() {
        try {
            if (model.getSocket() != null && !model.getSocket().isClosed()) {
                model.getSocket().close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
