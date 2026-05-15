<?php
// Izinkan akses dari mana saja (CORS)
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST");

// Hubungkan ke file konfigurasi database kamu
require_once '../config/database.php'; 

// Baca data JSON yang dikirim dari Android (Retrofit)
$data = json_decode(file_get_contents("php://input"));

// Pastikan datanya tidak kosong
if (!empty($data->id_pendamping) && !empty($data->nama_anak) && !empty($data->usia)) {
    
    // Ambil nilai dari JSON
    $id_pendamping = $data->id_pendamping;
    $nama_anak = $data->nama_anak;
    $usia = $data->usia;

    // Siapkan query memakai tabel tb_anak
    $query = "INSERT INTO tb_anak (id_pendamping, nama_anak, usia) VALUES (:id_pendamping, :nama_anak, :usia)";
    $stmt = $db->prepare($query);

    // Bind data ke query
    $stmt->bindParam(':id_pendamping', $id_pendamping);
    $stmt->bindParam(':nama_anak', $nama_anak);
    $stmt->bindParam(':usia', $usia);

    // Eksekusi query
    if ($stmt->execute()) {
        http_response_code(201); // 201 Created
        echo json_encode(array(
            "status" => "success",
            "message" => "Data anak berhasil ditambahkan."
        ));
    } else {
        http_response_code(503); // 503 Service Unavailable
        echo json_encode(array(
            "status" => "error",
            "message" => "Gagal menyimpan data anak."
        ));
    }
} else {
    // Jika ada data yang terlewat
    http_response_code(400); // 400 Bad Request
    echo json_encode(array(
        "status" => "error",
        "message" => "Data tidak lengkap. Pastikan ID Pendamping, Nama Anak, dan Usia terisi."
    ));
}
?>