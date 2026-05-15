<?php
class Pendamping {
    private $conn;
    private $table_name = "tb_pendamping";

    // Properti sesuai tabel tb_pendamping di database
    public $id_pendamping;
    public $nama_pendamping;
    public $email;
    public $password;
    public $pin_keamanan;
    public $peran;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Fungsi untuk registrasi pendamping baru
    public function register() {
        $query = "INSERT INTO " . $this->table_name . " 
                SET nama_pendamping=:nama, email=:email, password=:password, peran=:peran";

        $stmt = $this->conn->prepare($query);

        // Hash password untuk keamanan
        $this->password = password_hash($this->password, PASSWORD_BCRYPT);

        // Bind data
        $stmt->bindParam(":nama", $this->nama_pendamping);
        $stmt->bindParam(":email", $this->email);
        $stmt->bindParam(":password", $this->password);
        $stmt->bindParam(":peran", $this->peran);

        if($stmt->execute()) {
            return true;
        }
        return false;
    }
}
?>