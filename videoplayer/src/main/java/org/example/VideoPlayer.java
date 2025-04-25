package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class VideoPlayer {

    public static void main(String[] args) {
        try {
            // Установка FlatLaf Light Material Design стиля
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Не удалось установить FlatLaf: " + e.getMessage());
        }

        // Убедитесь, что путь к VLC установлен
        System.setProperty("jna.library.path", "C:\\Program Files\\VideoLAN\\VLC");

        // Создаем главное окно
        JFrame frame = new JFrame("Video Player");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Создаем компонент видеоплеера
        EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        frame.setContentPane(mediaPlayerComponent);

        // Панель управления
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

        // Панель времени и полосы прогресса
        JPanel progressPanel = new JPanel(new BorderLayout());

        JLabel currentTimeLabel = new JLabel("00:00");
        JLabel durationLabel = new JLabel("00:00");

        JSlider progressBar = new JSlider(0, 100);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(600, 20));

        // Линия времени и прогресса
        JPanel timeLinePanel = new JPanel(new BorderLayout());
        timeLinePanel.add(currentTimeLabel, BorderLayout.WEST);
        timeLinePanel.add(progressBar, BorderLayout.CENTER);
        timeLinePanel.add(durationLabel, BorderLayout.EAST);

        progressPanel.add(timeLinePanel, BorderLayout.NORTH);

        // Панель кнопок управления
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton playButton = new JButton(new ImageIcon(VideoPlayer.class.getResource("/icons/play.png")));
        JButton pauseButton = new JButton(new ImageIcon(VideoPlayer.class.getResource("/icons/pause.png")));
        JButton stopButton = new JButton(new ImageIcon(VideoPlayer.class.getResource("/icons/stop.png")));

        buttonsPanel.add(playButton);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(stopButton);

        // Добавляем панели управления в основной контейнер
        controls.add(progressPanel);
        controls.add(buttonsPanel);

        frame.add(controls, BorderLayout.SOUTH);

        // Добавляем возможность Drag and Drop
        new DropTarget(frame, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                // Не требуется действия
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                // Не требуется действия
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
                // Не требуется действия
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                // Не требуется действия
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : droppedFiles) {
                        // Проверяем, является ли файл видеофайлом
                        if (file.isFile() && file.getName().endsWith(".mp4")) {
                            mediaPlayerComponent.mediaPlayer().media().start(file.getAbsolutePath());
                        } else {
                            JOptionPane.showMessageDialog(frame, "Файл не является видеофайлом!");
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Привязка кнопок управления
        playButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().play());
        pauseButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
        stopButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().stop());

        // Таймер для обновления полосы прогресса и времени
        Timer timer = new Timer(500, e -> {
            if (!progressBar.getValueIsAdjusting() && mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
                long total = mediaPlayerComponent.mediaPlayer().media().info().duration();
                long current = mediaPlayerComponent.mediaPlayer().status().time();

                // Обновляем слайдер
                progressBar.setValue((int) (100 * current / total));

                // Обновляем метки времени
                currentTimeLabel.setText(formatTime(current));
                durationLabel.setText(formatTime(total));
            }
        });
        timer.start();

        // Обработка перемотки слайдером
        progressBar.addChangeListener(new ChangeListener() {
            private boolean isDragging = false;

            @Override
            public void stateChanged(ChangeEvent e) {
                if (progressBar.getValueIsAdjusting()) {
                    // Пользователь взаимодействует с слайдером
                    isDragging = true;
                } else if (isDragging) {
                    // Пользователь завершил взаимодействие
                    isDragging = false;
                    long total = mediaPlayerComponent.mediaPlayer().media().info().duration();
                    long newTime = total * progressBar.getValue() / 100;
                    mediaPlayerComponent.mediaPlayer().controls().setTime(newTime);
                }
            }
        });

        // Обработка кликов на слайдере
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mouseX = e.getX();
                int progressBarWidth = progressBar.getWidth();
                double percent = (double) mouseX / (double) progressBarWidth;

                long total = mediaPlayerComponent.mediaPlayer().media().info().duration();
                long newTime = (long) (total * percent);

                // Перемещаем видео и обновляем положение слайдера
                mediaPlayerComponent.mediaPlayer().controls().setTime(newTime);
                progressBar.setValue((int) (percent * 100));
            }
        });

        // Отображаем окно
        frame.setVisible(true);
    }

    // Метод для форматирования времени в формате mm:ss
    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}