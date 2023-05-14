// Package declaration
package application;



// Import necessary classes and libraries
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;

// Create a class for the Animated Meme Generator that extends JFrame and implements ActionListener

public class AnimatedMemeGenerator extends JFrame implements ActionListener {
    
	// Declare necessary variables and components
    private BufferedImage image;
    private JSlider numFramesSlider;
    private JSlider typingSpeedSlider;
    private JTextField messageField;

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AnimatedMemeGenerator().setVisible(true);
        });
    }

    // Constructor for the Animated Meme Generator class
    public AnimatedMemeGenerator() {
        // Set the title, size, and default close operation for the JFrame
        setTitle("Animated Meme Generator");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JPanel with GridBagLayout and GridBagConstraints for positioning components
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add an "Open Image" button to the panel
        JButton openButton = new JButton("Open Image");
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(openButton, gbc);
        // Add an ActionListener to the openButton
        openButton.addActionListener(this);
        
        // Add a message field to enter the text for the meme
        messageField = new JTextField(20);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(messageField, gbc);

        
        // Add a slider to adjust the number of frames for the animation
        numFramesSlider = new JSlider(2, 20, 10);
        numFramesSlider.setMajorTickSpacing(2);
        numFramesSlider.setMinorTickSpacing(1);
        numFramesSlider.setPaintTicks(true);
        numFramesSlider.setPaintLabels(true);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(numFramesSlider, gbc);
        
        // Add a slider to adjust the typing speed of the animation
        typingSpeedSlider = new JSlider(2, 20, 10);
        typingSpeedSlider.setMajorTickSpacing(2);
        typingSpeedSlider.setMinorTickSpacing(1);
        typingSpeedSlider.setPaintTicks(true);
        typingSpeedSlider.setPaintLabels(true);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(typingSpeedSlider, gbc);

        // Add a "Save GIF" button to the panel
        JButton saveButton = new JButton("Save GIF");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(saveButton, gbc);
        // Add an ActionListener to the saveButton
        saveButton.addActionListener(this);

        // Add the panel to the JFrame
        add(panel);
    }

    // Override the actionPerformed method for the ActionListener
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        // Perform different actions based on the button clicked
        if (command.equals("Open Image")) {
                        // Call the loadImage method to open an image file
            loadImage();
        } else if (command.equals("Save GIF")) {
            // Call the saveGif method to save the generated animation as a GIF
            try {
                saveGif();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method to open an image file and load it into the BufferedImage
    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error reading image file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to save the generated animation as a GIF
    private void saveGif() throws IOException {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String filePath = file.getPath();
            if (!filePath.toLowerCase().endsWith(".gif")) {
                file = new File(filePath + ".gif");
            }
            int typingSpeed = typingSpeedSlider.getValue();
            createAndSaveGif(file, typingSpeed);
        }
    }

    // Method to create and save the GIF file
    public void createAndSaveGif(File file, int typingSpeed) throws IOException {
        if (image != null && !messageField.getText().isEmpty()) {
            String message = messageField.getText();
            int numFrames = message.length() * typingSpeed;

            BufferedImage[] frames = new BufferedImage[numFrames];

            // Create frames with typing effect
            for (int i = 0; i < numFrames; i++) {
                int frameIndex = i / typingSpeed;
                frames[i] = createFrameWithTypingEffect(image, message, frameIndex % message.length());
            }

            // Save the GIF with the updated frames
            try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(file)) {
                ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/gif").next();

                writer.setOutput(outputStream);
                writer.prepareWriteSequence(null);

                int totalDuration = 2000; // milliseconds
                int delay = totalDuration / numFrames;
                for (BufferedImage frame : frames) {
                    ImageWriteParam iwp = writer.getDefaultWriteParam();
                    IIOMetadata metadata = createMetadata(writer, iwp, delay, frame);
                    writer.writeToSequence(new IIOImage(frame, null, metadata), iwp);
                }

                writer.endWriteSequence();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please load an image and enter a message before saving the GIF.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to create a single frame with the typing effect
    private BufferedImage createFrameWithTypingEffect(BufferedImage img, String message, int frameIndex) {
        BufferedImage frame = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = frame.createGraphics();
        g2.drawImage(img, 0, 0, null);

        Font font = new Font("Arial", Font.BOLD, 48);
        g2.setFont(font);
        g2.setColor(Color.WHITE);

        int maxCharacters = Math.min(frameIndex + 1, message.length());
        String partialMessage = message.substring(0, maxCharacters);

        int x = 300;
        int y = img.getHeight() - 550;
        g2.drawString(partialMessage, x, y);

                // Dispose of the Graphics2D object after drawing the text on the frame
        g2.dispose();
        return frame;
    }

    // Method to create metadata for the GIF, including the delay between frames
    private IIOMetadata createMetadata(ImageWriter writer, ImageWriteParam iwp, int delay, BufferedImage image) throws IOException {
        IIOMetadata metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), iwp);
        String nativeFormatName = metadata.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(nativeFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));

        metadata.setFromTree(nativeFormatName, root);

        return metadata;
    }

    // Method to get a specific metadata node or create a new one if it doesn't exist
    protected static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}