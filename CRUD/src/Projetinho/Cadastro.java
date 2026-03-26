/*Caique França || SP3118541
 Fernando Duarte || SP311872X
 * */

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
            txtTelefone = new JTextField(),
            txtCidade = new JTextField();

    // Botões
    private JButton btnSalvar = new JButton("Salvar"),
            btnAtualizar = new JButton("Atualizar"),
            btnExcluir = new JButton("Excluir"),
            btnLimpar = new JButton("Limpar");

    // Tabela e modelo
    private JTable tabela;
    private DefaultTableModel modelo;

    // Construtor da tela
    public Cadastro() {
        setTitle("Cadastro de Clientes");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ID não pode ser editado (auto incremento)
        txtId.setEditable(false);

        // Inicialização
        criarTabelaBanco();
        inicializarComponentes();
        listarClientes();
    }

    // Conexão com banco SQLite
    private Connection conectar() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:Clientes.db");
    }

    // Executa comandos SQL simples (CREATE, etc.)
    private void executarSQL(String sql) {
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Cria tabela caso não exista
    private void criarTabelaBanco() {
        executarSQL("CREATE TABLE IF NOT EXISTS clientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT NOT NULL," +
                "telefone TEXT NOT NULL," +
                "cidade TEXT NOT NULL)");
    }

    // Inicializa interface gráfica
    private void inicializarComponentes() {

        // Formulário
        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));

        form.add(new JLabel("ID:"));
        form.add(txtId);

        form.add(new JLabel("Nome:"));
        form.add(txtNome);

        form.add(new JLabel("Telefone:"));
        form.add(txtTelefone);

        form.add(new JLabel("Cidade:"));
        form.add(txtCidade);

        // Botões principais
        form.add(btnSalvar);
        form.add(btnAtualizar);

        // Painel superior
        JPanel sul = new JPanel(new BorderLayout());
        sul.add(form, BorderLayout.CENTER);

        // Botões inferiores
        JPanel botoes = new JPanel();
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);

        sul.add(botoes, BorderLayout.SOUTH);

        add(sul, BorderLayout.NORTH);

        // Modelo da tabela com colunas definidas
        modelo = new DefaultTableModel(new String[]{"ID", "Nome", "Telefone", "Cidade"}, 0);

        // Tabela
        tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Eventos dos botões
        btnSalvar.addActionListener(e -> salvarCliente());
        btnAtualizar.addActionListener(e -> atualizarCliente());
        btnExcluir.addActionListener(e -> excluirCliente());
        btnLimpar.addActionListener(e -> limparCampos());

        // Evento de clique na tabela
        tabela.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                carregarCamposDaTabela();
            }
        });
    }

    // Validação de campos
    private boolean camposValidos() {
        if (txtNome.getText().trim().isEmpty() ||
                txtTelefone.getText().trim().isEmpty() ||
                txtCidade.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
            return false;
        }
        return true;
    }

    // Inserir novo cliente
    private void salvarCliente() {
        if (!camposValidos()) return;

        String sql = "INSERT INTO clientes (nome, telefone, cidade) VALUES (?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtNome.getText().trim());
            stmt.setString(2, txtTelefone.getText().trim());
            stmt.setString(3, txtCidade.getText().trim());
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso.");
            limparCampos();
            listarClientes();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Listar clientes na tabela
    private void listarClientes() {
        modelo.setRowCount(0);

        String sql = "SELECT * FROM clientes";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("telefone"),
                        rs.getString("cidade")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Carrega dados da tabela para os campos
    private void carregarCamposDaTabela() {
        int linha = tabela.getSelectedRow();
        if (linha != -1) {
            txtId.setText(modelo.getValueAt(linha, 0).toString());
            txtNome.setText(modelo.getValueAt(linha, 1).toString());
            txtTelefone.setText(modelo.getValueAt(linha, 2).toString());
            txtCidade.setText(modelo.getValueAt(linha, 3).toString());
        }
    }

    // Atualiza cliente selecionado
    private void atualizarCliente() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente.");
            return;
        }

        if (!camposValidos()) return;

        String sql = "UPDATE clientes SET nome=?, telefone=?, cidade=? WHERE id=?";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, txtNome.getText().trim());
            stmt.setString(2, txtTelefone.getText().trim());
            stmt.setString(3, txtCidade.getText().trim());
            stmt.setInt(4, Integer.parseInt(txtId.getText()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Atualizado com sucesso.");
            limparCampos();
            listarClientes();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Excluir cliente
    private void excluirCliente() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente.");
            return;
        }

        // Confirmação
        if (JOptionPane.showConfirmDialog(this, "Deseja excluir?", "Confirmação",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM clientes WHERE id=?";

        try (Connection conn = conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, Integer.parseInt(txtId.getText()));
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Excluído com sucesso.");
            limparCampos();
            listarClientes();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Limpa formulário
    private void limparCampos() {
        txtId.setText("");
        txtNome.setText("");
        txtTelefone.setText("");
        txtCidade.setText("");
        tabela.clearSelection();
    }

    // Método principal
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cadastro().setVisible(true));
    }
}