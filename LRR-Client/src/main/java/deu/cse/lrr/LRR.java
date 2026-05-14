/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package deu.cse.lrr;

import deu.cse.lrr.login.LoginController;
import deu.cse.lrr.login.LoginModel;
import deu.cse.lrr.login.LoginView;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author seo
 */
public class LRR {

    private static final Logger logger = Logger.getLogger(LRR.class.getName());

    public static void main(String[] args) {
        // Nimbus Look and Feel 설정
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        // Login JFrame 실행
        java.awt.EventQueue.invokeLater(() -> {
            LoginView view = new LoginView();
            LoginModel model = new LoginModel();
            new LoginController(view, model);
            view.setVisible(true);
        });
    }
}
