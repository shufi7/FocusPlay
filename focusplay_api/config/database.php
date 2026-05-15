<?php
class Database {
    private $host = "localhost";
    private $db_name = "db_focusplay_app"; // Sesuai dengan nama database kamu
    private $username = "root";
    private $password = ""; 
    public $conn;

    public function getConnection() {
        $this->conn = null;
        try {
            $this->conn = new PDO("mysql:host=" . $this->host . ";dbname=" . $this->db_name, $this->username, $this->password);
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            $this->conn->exec("set names utf8");
            
            // Baris ini hanya untuk testing sementara, nanti bisa dihapus
            // echo "Koneksi ke db_focusplay_app Berhasil!"; 
            
        } catch(PDOException $exception) {
            echo "Koneksi database gagal: " . $exception->getMessage();
        }
        return $this->conn;
    }
}

// Eksekusi testing sementara
$test = new Database();
$test->getConnection();
?>