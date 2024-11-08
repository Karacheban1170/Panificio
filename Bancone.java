import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Bancone extends JPanel implements Runnable, MouseListener {

    private Thread updateThread;
    private boolean running = true;

    private final PanificioMonitor panificioMonitor;

    private final int width, height;

    private final BufferedImage sfondo;
    private final BufferedImage bancone;
    private final BufferedImage btnIndietro;
    private final BufferedImage btnForno;
    private final Rectangle btnIndietroBounds;
    private final Rectangle btnFornoBounds;

    private ArrayList<Cliente> clienti;

    private final ArrayList<Prodotto> prodotti;
    private final ArrayList<Rectangle> prodottiBounds;
    private final int quantita;

    private final BufferedImage[] immaginiClienti = {
        ImageLoader.loadImage("img/cliente1.png"),
        ImageLoader.loadImage("img/cliente2.png"),
        ImageLoader.loadImage("img/cliente3.png"),
        ImageLoader.loadImage("img/cliente4.png"),
        ImageLoader.loadImage("img/cliente5.png"),
        ImageLoader.loadImage("img/cliente6.png")
    };

    private final ActionListener toPnlPanificioAction;
    private final ActionListener toPnlFornoAction;

    public Bancone(int width, int height, ActionListener toPnlPanificioAction, ActionListener toPnlFornoAction) {
        this.width = width;
        this.height = height;
        this.sfondo = ImageLoader.loadImage("img/bancone_sfondo.jpg");
        this.bancone = ImageLoader.loadImage("img/bancone.png");
        this.btnIndietro = ImageLoader.loadImage("img/btn_indietro.png");
        this.btnForno = ImageLoader.loadImage("img/btn_forno.png");

        panificioMonitor = new PanificioMonitor();

        prodotti = new ArrayList<>(4);
        prodottiBounds = new ArrayList<>(prodotti.size());

        quantita = 5;
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto1.png"), "ciambella", quantita));
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto2.png"),"croissant", quantita));
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto3.png"),"muffin", quantita));
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto4.png"),"pane", quantita));

        btnIndietroBounds = new Rectangle(width - 215, 22, 180, 60);
        btnFornoBounds = new Rectangle(width - 181, 392, 105, 105);

        this.toPnlPanificioAction = toPnlPanificioAction;
        this.toPnlFornoAction = toPnlFornoAction;
        addMouseListener(this);

        DynamicCursor.setCustomCursors(this);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(10);
                DynamicCursor.updateCursor(this, btnIndietroBounds, btnFornoBounds, prodottiBounds);
                repaint();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public synchronized void start() {
        if (updateThread == null || !updateThread.isAlive()) {
            running = true;
            updateThread = new Thread(this);
            updateThread.start();
        }
    }

    public synchronized void stop() {
        running = false;
        if (updateThread != null) {
            updateThread.interrupt();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        disegnaSfondo(g2d);

        for (Cliente cliente : clienti) {
            cliente.disegnaCliente(g2d);
            cliente.disegnaNuvola(g2d);
            cliente.disegnaProdottoDesiderato(g2d);
            cliente.disegnaProgressBar(g2d);
        }

        disegnaBancone(g2d);
        disegnaProdotti(g2d);
        disegnaBtnIndietro(g2d);
        disegnaBtnForno(g2d);

        FadingScene.disegnaFadingRect(g2d);
    }

    private void disegnaSfondo(Graphics2D g2d) {
        if (sfondo != null) {
            g2d.drawImage(sfondo, 0, 0, width, height, this);
        } else {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, width, height);
        }
    }

    private void disegnaBancone(Graphics2D g2d) {
        if (bancone != null) {
            g2d.drawImage(bancone, 0, 355, bancone.getWidth(), bancone.getHeight(), this);
        } else {
            g2d.setColor(Color.CYAN);
            g2d.fillRect(0, 355, width, height);
        }
    }

    private void disegnaProdotti(Graphics2D g2d) {
        int xPos = width / 2 - 180;
        int yPos = 415;

        for (int i = 0; i < prodotti.size(); i++) {
            if (prodotti.get(i) != null) {
                // Disegna il prodotto
                g2d.drawImage(prodotti.get(i).getImage(), xPos, yPos, 50, 50, this);

                // Aggiungi il Rectangle per il prodotto
                prodottiBounds.add(new Rectangle(xPos, yPos, 50, 50));

                // Disegna la quantità accanto al prodotto
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String quantitaStr = String.valueOf(prodotti.get(i).getQuantita());
                g2d.drawString(quantitaStr, xPos + 55, yPos + 50);

                xPos += 100;
            }
        }
    }

    private void disegnaBtnIndietro(Graphics2D g2d) {
        if (btnIndietro != null) {
            g2d.drawImage(btnIndietro, width - btnIndietro.getWidth() - 40, 20, btnIndietro.getWidth(),
                    btnIndietro.getHeight(),
                    this);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(width - 40, 20, width, height);
        }
    }

    private void disegnaBtnForno(Graphics2D g2d) {
        if (btnForno != null) {
            g2d.drawImage(btnForno, btnForno.getWidth() + 270, btnForno.getHeight() - 118, 125, 125,
                    this);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(width - 40, 100, width, height);
        }
    }

    public void initClienti() {
        clienti = new ArrayList<>();
        Random rand = new Random();
        ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            Cliente cliente = new Cliente(immaginiClienti[rand.nextInt(immaginiClienti.length)],
                    panificioMonitor, "Cliente" + (i + 1), prodotti);
            clienti.add(cliente);

            Thread th = new Thread(cliente);
            threads.add(th);
        }

        for (Thread th : threads) {
            th.start();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        requestFocusInWindow();

        Point mousePosition = e.getPoint();

        // Controlla se il clic è su uno dei prodotti
        for (int i = 0; i < prodottiBounds.size(); i++) {
            if (prodottiBounds.get(i).contains(mousePosition)) {
                prodotti.get(i).decrementaQuantita();
                break;
            }
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Point mousePosition = e.getPoint();
            if (DynamicCursor.isMouseOverBounds(mousePosition, btnIndietroBounds)) {
                if (toPnlPanificioAction != null && PanificioMonitor.isClientiEntrano() == false) {
                    toPnlPanificioAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                }

            } else if (DynamicCursor.isMouseOverBounds(mousePosition, btnFornoBounds)) {
                if (toPnlFornoAction != null) {
                    toPnlFornoAction.actionPerformed(new ActionEvent(this,
                            ActionEvent.ACTION_PERFORMED, null));
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}