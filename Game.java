import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class Game extends JFrame {

    final String TITLE_OF_PROGRAM = "Tetris";
    final int BLOCK_SIZE = 25; // размер блока
    final int ARC_RADIUS = 6; // значение закругления фируры
    final int FIELD_WIDTH = 10; // ширина
    final int FIELD_HEIGHT = 18; // и высота поля в блоках
    final int START_LOCATION = 180; // начальное расположение фигуры
    final int FIELD_DX = 7; // специальная константа
    final int FIELD_DY = 26; // специальная константа
    final int LEFT = 37; // клавиша влево
    final int UP = 38; // вверх
    final int RIGHT = 39; // вправо
    final int DOWN = 40; // вниз
    final int SHOW_DELAY = 300; // задержка анимации
    final int[][][] SHAPES = {
            {{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {4, 0x00f0f0}}, // I
            {{0, 0, 0, 0}, {0, 1, 1, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {4, 0xf0f000}}, // O
            {{1, 0, 0, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x0000f0}}, // J
            {{0, 0, 1, 0}, {1, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xf0a000}}, // L
            {{0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0x00f000}}, // S
            {{1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xa000f0}}, // T
            {{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {3, 0xf00000}}  // Z
    }; // трехмерный массив заготовок фигур
    final int[] SCORES = {100, 300, 700, 1500}; // очки за уничтожение 1,2,3 и 4 строчек
    int gameScore = 0; //хранитель очков
    int[][] mine = new int[FIELD_HEIGHT + 1][FIELD_WIDTH]; //двухмерный масив поля
    JFrame frame;
    Canvas canvas = new Canvas(); //панель для отрисовки
    Random random = new Random();
    Figure figure = new Figure();
    boolean gameOver = false; //определяет закончена ли игра
    final int[][] GAME_OVER_MSG = {
            {0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 0},
            {1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 1, 1},
            {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0},
            {0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 0},
            {1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0},
            {1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0},
            {1, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0},
            {0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0}}; // отрисовка фразы "Game Over"

    public static void main(String[] args) {

        new Game().go();
    }

    void go() {
        frame = new JFrame(TITLE_OF_PROGRAM); // новое окно
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // позволяет закрывать окно нажатием красного крестика
        frame.setSize(FIELD_WIDTH * BLOCK_SIZE + FIELD_DX, FIELD_HEIGHT * BLOCK_SIZE + FIELD_DY); // задаем размер окна
        frame.setLocation(START_LOCATION, START_LOCATION); //начальное положение окна
        frame.setResizable(false); // размер менять нельзя
        canvas.setBackground(Color.black); // цвет бэкграунда - черный

        frame.addKeyListener(new KeyAdapter() { // обработчик нажатия клавиш
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    if (e.getKeyCode() == DOWN) figure.drop();
                    if (e.getKeyCode() == UP) figure.rotate();
                    if (e.getKeyCode() == LEFT || e.getKeyCode() == RIGHT) figure.move(e.getKeyCode());
                }
                canvas.repaint();
            }
        });
        frame.getContentPane().add(BorderLayout.CENTER, canvas);
        frame.setVisible(true); // окно видимое

        Arrays.fill(mine[FIELD_HEIGHT], 1); // прорисовка дна

        // Главный цикл игры
        while (!gameOver) {
            try {
                Thread.sleep(SHOW_DELAY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.repaint();
            if (figure.isTouchGround()) {
                figure.leaveOnTheGround();
                checkFilling(); // проверить состояние
                figure = new Figure(); // создаетс новая фигура
                gameOver = figure.isCrossGround();
            } else {
                figure.stepDown();
            }
        }
    }

    void checkFilling() { // проверка на запоненую полосу и удаление ее
        int row = FIELD_HEIGHT - 1;
        int countFillRows = 0;
        while (row > 0) {
            int filled = 1;
            for (int col = 0; col < FIELD_WIDTH; col++)
                filled *= Integer.signum(mine[row][col]);
            if (filled > 0) {
                countFillRows++;
                for (int i = row; i > 0; i--) System.arraycopy(mine[i-1], 0, mine[i], 0, FIELD_WIDTH);
            } else
                row--;
        }
        if (countFillRows > 0) { // начисление баллов за удаленную строку и отображение баллов в заголовке
            gameScore += SCORES[countFillRows - 1];
            setTitle(TITLE_OF_PROGRAM + " : " + gameScore);
        }
    }

    public class Figure {
        private ArrayList<Block> figure = new ArrayList<Block>(); //
        private int[][] shape = new int[4][4]; 
        private int type, size, color; //
        private int x = 3, y = 0;

        Figure() {
            type = random.nextInt(SHAPES.length);
            size = SHAPES[type][4][0];
            color = SHAPES[type][4][1];
            if (size == 4) {
                y = -1;
            }
            for (int i = 0; i < size; i++) {
                System.arraycopy(SHAPES[type][i], 0, shape[i], 0, SHAPES[type][i].length);
            }
            createFromShape();
        }

        void createFromShape() {
            for (int x = 0; x < size; x++)
                for (int y = 0; y < size; y++)
                    if (shape[y][x] == 1) {
                        figure.add(new Block(x + this.x, y + this.y));
                    }
        }

        boolean isTouchGround() { 		//проверка на дотронулась ли фигура до поверхности
		for (Block block : figure) if (mine[block.getY() + 1][block.getX()] > 0) return true;
            return false;
        }

        boolean isCrossGround() { // не пересекла ли созданая фигура "землю" (верхнюю поверхность)
        for (Block block : figure) if (mine[block.getY()][block.getX()] > 0) return true;
		return false;
        }

        void leaveOnTheGround() { // оставить на поверхности
        for (Block block : figure) mine[block.getY()][block.getX()] = color; // изменяется цвет над поверхностью на цвет фигуры
        }

        void stepDown() { // движение вниз
        for (Block block : figure) block.setY (block.getY() + 1); // изменение координаты у
		y++;
        }

        void drop() { // метод сброса вниз
        while (!isTouchGround())stepDown();
        }
        boolean isTouchWall(int  direction){ // проверяет что б фигура не проходила через стену
			for (Block block : figure){
			if (direction == LEFT && (block.getX() == 0 || mine[block.getY() ][block.getX() - 1] > 0)) return true;
			if (direction == RIGHT && (block.getX() == FIELD_WIDTH - 1 || mine[block.getY() ][block.getX() + 1] > 0)) return true;
			}
			return false;
		}
        void move(int direction) { // метод сдвига
            if (!isTouchWall(direction)){
			    int dx = direction - 38;
			    for (Block block : figure) block.setX(block.getX() + dx);
			    x += dx;
		    }
        }
        boolean isWrongPosition() { // проверка на возможность фигуры при повороте войти в текстуры 
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++)
					if (shape[y][x] == 1){
						if (y + this.y < 0) return true;
						if (x + this.x < 0 || x + this.x > FIELD_WIDTH - 1) return true;
						if (mine[y + this.y][x + this.x] > 0) return true;
					}
			return false;		
		}
        void rotate() { // метод вращения
            for (int i = 0; i < size/2; i++)
			    for (int j = i; j < size - 1 - i; j++) {
				    int tmp = shape[size - 1 - j][i];   //shape - массив в котором образ фигуры
				    shape[size - 1 - j][i] = shape[size - 1 - i][size - 1 - j];
				    shape[size - 1 - i][size - 1 - j] = shape[j][size - 1 - i];
				    shape[j][size - 1 - i] = shape[i][j]; 
				    shape[i][j] = tmp;
			    }
		    if(!isWrongPosition()){ // если при повороте фигура не попадает на текстуры,то
			    figure.clear(); // фигура удаляется
			    createFromShape(); // и создается по образу массива shape, а он уже повернут
		    }
        }

        void paint(Graphics g) {
            for (Block block : figure) {
                block.paint(g, color);
            }
        }
    }

    class Block{  
        private int x, y;

        public Block(int x, int y) {
            setX(x);
            setY(y);
        }

        void setX(int x) {
            this.x = x;
        }

        void setY(int y) {
            this.y = y;
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        void paint(Graphics g, int color) {
            g.setColor(new Color(color));
            g.drawRoundRect(x * BLOCK_SIZE + 1, y * BLOCK_SIZE + 1, BLOCK_SIZE - 2, BLOCK_SIZE - 2, ARC_RADIUS, ARC_RADIUS);
        }
    }

    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
			for (int x = 0; x < FIELD_WIDTH; x++)          // отрисовывает 
				for (int y = 0; y < FIELD_HEIGHT; y++)       // фигуры
					if (mine [y][x] > 0){                      // на поверхности
						g.setColor(new Color(mine [y][x]));
						g.fill3DRect(x*BLOCK_SIZE+1, y*BLOCK_SIZE+1, BLOCK_SIZE-1, BLOCK_SIZE-1, true);
					}
			if(gameOver){
				g.setColor(Color.white);
				for(int y = 0; y < GAME_OVER_MSG.length; y++)
				    for(int x = 0; x < GAME_OVER_MSG[y].length; x++)
					    if(GAME_OVER_MSG[y][x] == 1) g.fill3DRect(x * 11 + 18, y * 11 + 160, 10, 10, true);
			}
			else	
            figure.paint(g);
        }
    }
}
