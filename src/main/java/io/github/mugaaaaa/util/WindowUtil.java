package io.github.mugaaaaa.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

public class WindowUtil {

    /**
     * 打开一个新的模态窗口的通用函数。
     *
     * @param ownerWindow 父窗口.
     * @param fxmlPath    要加载的FXML文件的相对路径.
     * @param title       新窗口的标题.
     * @return            返回与FXML文件关联的控制器实例.
     */
    public static <T> T openModalWindow(Window ownerWindow, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL resource = WindowUtil.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("找不到地址为" + fxmlPath + "的FXML文件");
            }
            loader.setLocation(resource);

            // 加载FXML文件
            Parent root = loader.load();

            // 创建新的Stage
            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));

            // 设置窗口模态和所有者
            newStage.initModality(Modality.WINDOW_MODAL);
            newStage.initOwner(ownerWindow);

            // 窗口渲染进程持续
            newStage.showAndWait();

            return loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("不能打开窗口");
            return null;
        }
    }
}