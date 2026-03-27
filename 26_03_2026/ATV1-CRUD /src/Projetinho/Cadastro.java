/* Caique França || SP3118541*/

package Projetinho;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Cadastro extends JFrame {

    // Campos de texto
    private JTextField txtId = new JTextField(),
            txtNome = new JTextField(),
            txtPreco = new JTextField(),
            txtQuantidade = new JTextField();

    // Botões
    private JButton btnSalvar = new JButton("Salvar"),
            btnAtualizar = new JButton("Atualizar"),
            btnExcluir = new JButton("Excluir"),
            btnLimpar = new JButton("Limpar");

    // Tabela e modelo
    private JTable tabela;
    private DefaultTableModel modelo;

    // Construtor
    public Cadastro() {
        setTitle("Cadastro de Produtos");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        txtId.setEditable(false);

        criarTabelaBanco();
        inicializarComponentes();
        listarProdutos();
    }

    // Conexão SQLite
    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:Produtos.db");
    }

    // Executar SQL
    private void executarSQL(String sql) {
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Criar tabela
    private void criarTabelaBanco() {
        executarSQL("CREATE TABLE IF NOT EXISTS produtos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "preco REAL NOT NULL," +
                "quantidade INTEGER NOT NULL)");
    }

    // Interface
    private void inicializarComponentes() {

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));

        form.add(new JLabel("ID:"));
        form.add(txtId);

        form.add(new JLabel("Nome:"));
        form.add(txtNome);

        form.add(new JLabel("Preço:"));
        form.add(txtPreco);

        form.add(new JLabel("Quantidade:"));
        form.add(txtQuantidade);

        form.add(btnSalvar);
        form.add(btnAtualizar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(form, BorderLayout.CENTER);

        JPanel botoes = new JPanel();
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);

        topo.add(botoes, BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{"ID", "Nome", "Preço", "Quantidade"}, 0);

        tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Eventos
        btnSalvar.addActionListener(e -> salvarProduto());
        btnAtualizar.addActionListener(e -> atualizarProduto());
        btnExcluir.addActionListener(e -> excluirProduto());
        btnLimpar.addActionListener(e -> limparCampos());

        tabela.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                carregarCamposDaTabela();
            }
        });
    }

    // Validação
    private boolean camposValidos() {
        if (txtNome.getText().trim().isEmpty() ||
            txtPreco.getText().trim().isEmpty() ||
            txtQuantidade.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
            return false;
        }
        return true;
    }

    // Salvar
    private void salvarProduto() {
        if (!camposValidos()) return;

        String sql = "INSERT INTO produtos (nome, preco, quantidade) VALUES (?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtNome.getText().trim());
            stmt.setDouble(2, Double.parseDouble(txtPreco.getText().trim()));
            stmt.setInt(3, Integer.parseInt(txtQuantidade.getText().trim()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso.");
            limparCampos();
            listarProdutos();

        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    // Listar
    private void listarProdutos() {
        modelo.setRowCount(0);

        String sql = "SELECT * FROM produtos";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getInt("quantidade")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Carregar da tabela
    private void carregarCamposDaTabela() {
        int linha = tabela.getSelectedRow();
        if (linha != -1) {
            txtId.setText(modelo.getValueAt(linha, 0).toString());
            txtNome.setText(modelo.getValueAt(linha, 1).toString());
            txtPreco.setText(modelo.getValueAt(linha, 2).toString());
            txtQuantidade.setText(modelo.getValueAt(linha, 3).toString());
        }
    }

    // Atualizar
    private void atualizarProduto() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto.");
            return;
        }

        if (!camposValidos()) return;

        String sql = "UPDATE produtos SET nome=?, preco=?, quantidade=? WHERE id=?";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtNome.getText().trim());
            stmt.setDouble(2, Double.parseDouble(txtPreco.getText().trim()));
            stmt.setInt(3, Integer.parseInt(txtQuantidade.getText().trim()));
            stmt.setInt(4, Integer.parseInt(txtId.getText()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Atualizado com sucesso.");
            limparCampos();
            listarProdutos();

        } catch (SQLException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    // Excluir
    private void excluirProduto() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um produto.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Deseja excluir?", "Confirmação",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM produtos WHERE id=?";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(txtId.getText()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Excluído com sucesso.");
            limparCampos();
            listarProdutos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Limpar
    private void limparCampos() {
        txtId.setText("");
        txtNome.setText("");
        txtPreco.setText("");
        txtQuantidade.setText("");
        tabela.clearSelection();
    }

    // Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cadastro().setVisible(true));
    }
}
