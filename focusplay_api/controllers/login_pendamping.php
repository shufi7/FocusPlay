<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

include_once '../config/database.php';
include_once '../models/Pendamping.php';

$database = new Database();
$db = $database->getConnection();

$pendamping = new Pendamping($db);

// Ambil input JSON dari aplikasi Android
$data = json_decode(file_get_contents("php://input"));

if(!empty($data->email) && !empty($data->password)) {
    $pendamping->email = $data->email;
    $pendamping->password = $data->password;

    // Eksekusi fungsi login
    if($pendamping->login()) {
        http_response_code(200); // OK
        echo json_encode(array(
            "status" => "success",
            "message" => "Login berhasil!",
            "data" => array(
                "id_pendamping" => $pendamping->id_pendamping,
                "nama_pendamping" => $pendamping->nama_pendamping,
                "email" => $pendamping->email,
                "peran" => $pendamping->peran
            )
        ));
    } else {
        http_response_code(401); // Unauthorized
        echo json_encode(array("status" => "error", "message" => "Email atau password salah."));
    }
} else {
    http_response_code(400); // Bad Request
    echo json_encode(array("status" => "error", "message" => "Data tidak lengkap."));
}
?>