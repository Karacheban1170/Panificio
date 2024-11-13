import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
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

    private static ArrayList<Cliente> clienti;
    private final int numClienti;

    private static ArrayList<Prodotto> prodotti;
    private static BufferedImage prodottoFrame;
    private final ArrayList<Rectangle> prodottiBounds;
    private final int numProdotti;
    private final int quantitaProdotti;

    private static int score;

    private static final Color WHITE_COLOR = new Color(232, 247, 238, 255);
    private static final Color BROWN_COLOR = new Color(46, 21, 0, 255);

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

    private final GestioneAudio clienteSoddisfatto;
    private final GestioneAudio clienteArrabbiato;

    public Bancone(int width, int height, ActionListener toPnlPanificioAction, ActionListener toPnlFornoAction) {
        this.width = width;
        this.height = height;
        this.sfondo = ImageLoader.loadImage("img/bancone_sfondo.jpg");
        this.bancone = ImageLoader.loadImage("img/bancone.png");
        this.btnIndietro = ImageLoader.loadImage("img/btn_indietro.png");
        this.btnForno = ImageLoader.loadImage("img/btn_forno.png");

        panificioMonitor = new PanificioMonitor();

        numClienti = 10;
        numProdotti = 4;
        quantitaProdotti = 2;

        score = 0;

        prodotti = new ArrayList<>(numProdotti);
        prodottiBounds = new ArrayList<>(prodotti.size());

        prodottoFrame = ImageLoader.loadImage("img/prodottoFrame.png");
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto1.png"), "ciambella", quantitaProdotti));
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto2.png"), "croissant", quantitaProdotti));
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto3.png"), "muffin", quantitaProdotti));
        prodotti.add(new Prodotto(ImageLoader.loadImage("img/prodotto4.png"), "pane", quantitaProdotti));

        btnIndietroBounds = new Rectangle(width - 215, 22, 180, 60);
        btnFornoBounds = new Rectangle(width - 181, 392, 105, 105);

        this.toPnlPanificioAction = toPnlPanificioAction;
        this.toPnlFornoAction = toPnlFornoAction;

        clienteSoddisfatto = new GestioneAudio("audio/cliente_soddisfatto.wav");
        clienteSoddisfatto.setVolume(0.55f);

        clienteArrabbiato = new GestioneAudio("audio/cliente_arrabbiato.wav");
        clienteArrabbiato.setVolume(0.55f);

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
        int xPos = width / 2 - 195;
        int yPos = 405;
        int sizeProdotto = 80;

        for (int i = 0; i < prodotti.size(); i++) {
            if (prodotti.get(i) != null) {
                // Disegna la cornice del prodotto
                g2d.drawImage(prodottoFrame, xPos, yPos, sizeProdotto, sizeProdotto, this);

                // Disegna il prodotto
                g2d.drawImage(prodotti.get(i).getImage(), xPos, yPos, sizeProdotto, sizeProdotto, this);

                // Aggiungi il Rectangle per il prodotto
                prodottiBounds.add(new Rectangle(xPos, yPos, sizeProdotto, sizeProdotto));

                // Disegna la quantità accanto al prodotto
                g2d.setColor(BROWN_COLOR);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));

                String quantitaStr = String.valueOf(prodotti.get(i).getQuantita());
                g2d.fillOval(xPos + 58, yPos + 49, 31, 31);

                g2d.setColor(WHITE_COLOR);

                if (prodotti.get(i).getQuantita() > 9) {
                    g2d.drawString(quantitaStr, xPos + 65, yPos + 70);
                } else {
                    g2d.drawString(quantitaStr, xPos + 70, yPos + 70);
                }

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

        for (int i = 0; i < numClienti; i++) {

            Cliente cliente = new Cliente(immaginiClienti[rand.nextInt(immaginiClienti.length)],
                    panificioMonitor, "Cliente " + (i + 1), prodotti);
            clienti.add(cliente);

            Thread th = new Thread(cliente);
            threads.add(th);
        }

        for (Thread th : threads) {
            th.start();
        }
    }

    private void vendiProdotto(Point mousePosition) {
        for (int i = 0; i < prodottiBounds.size(); i++) {
            if (prodottiBounds.get(i).contains(mousePosition)) {
                Prodotto prodottoSelezionato = prodotti.get(i);
                if (prodottoSelezionato.getQuantita() == 0) {
                    break;
                }

                // Verifica se il cliente desidera questo prodotto
                for (Cliente cliente : clienti) {
                    if (cliente.isClienteAspetta()) {
                        cliente.setProdottoComprato(true);
                        if (cliente.getProdottoDesiderato().equals(prodottoSelezionato)) {
                            // Decrementa la quantità del prodotto
                            prodottoSelezionato.decrementaQuantita();
                            cliente.setSoddisfatto(true);
                            clienteSoddisfatto.playSound();
                            score += 20;
                            break;
                        } else {
                            prodottoSelezionato.decrementaQuantita();
                            cliente.setSoddisfatto(false);
                            clienteArrabbiato.playSound();
                            score += 5;
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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

                    try {
                        File file = new File("risultato_migliore.txt");
                        int migliorRisultato = 0;
                        try (Scanner sc = new Scanner(file)) {
                            if (sc.hasNextInt()) {
                                migliorRisultato = sc.nextInt();
                            }
                        }

                        if (getScore() > migliorRisultato) {
                            try (PrintWriter writer = new PrintWriter(file)) {
                                writer.println(getScore());
                            }
                        }

                    } catch (FileNotFoundException exc) {
                        exc.getMessage();
                    }
                }

            } else if (DynamicCursor.isMouseOverBounds(mousePosition, btnFornoBounds)) {
                if (toPnlFornoAction != null) {
                    toPnlFornoAction.actionPerformed(new ActionEvent(this,
                            ActionEvent.ACTION_PERFORMED, null));
                }
            }

            vendiProdotto(mousePosition);

        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * @return the prodotti
     */
    public static ArrayList<Prodotto> getProdotti() {
        return prodotti;
    }

    public static ArrayList<Cliente> getClienti() {
        return clienti;
    }

    public static BufferedImage getProdottoFrame() {
        return prodottoFrame;
    }

    public static void setScore(int score) {
        Bancone.score = score;
    }

    public static int getScore() {
        return score;
    }
}
