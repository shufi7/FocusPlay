<?php
// Header agar bisa diakses Android dan formatnya JSON
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

include_once '../config/database.php';
include_once '../models/Pendamping.php';

$database = new Database();
$db = $database->getConnection();

$pendamping = new Pendamping($db);

// Ambil data input dari Android (JSON)
$data = json_decode(file_get_contents("php://input"));

if(!empty($data->nama) && !empty($data->email) && !empty($data->password)) {
    $pendamping->nama_pendamping = $data->nama;
    $pendamping->email = $data->email;
    $pendamping->password = $data->password;
    $pendamping->peran = "Orang Tua"; // Default peran

    if($pendamping->register()) {
        echo json_encode(array("message" => "Registrasi berhasil!"));
    } else {
        echo json_encode(array("message" => "Registrasi gagal."));
    }
} else {
    echo json_encode(array("message" => "Data tidak lengkap."));
}
?>