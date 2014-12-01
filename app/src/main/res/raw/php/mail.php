<?php 
if(isset($_POST['Enviar']))
{
    $to = "uxds@abimobileapps.esy.es";
    $from = $_POST['email'];
    $name = $_POST['nombres_apellido'];
    $subject = "[UxDS] Su ficha de afiliación";

    $message = trim($name) . ", cargaste la siguiente ficha de afiliación:" . "\n\n";
    $message .= "Nombres y Apellido: " . $_POST['nombres_apellido']. "\n";
    $message .= "Domicilio: " . $_POST['domicilio']. "\n";
    $message .= "Teléfono: " . $_POST['telefono']. "\n";
    $message .= "CUIL: " . $_POST['cuil']. "\n";
    $message .= "Legajo: " . $_POST['legajo']. "\n";
    $message .= "Fecha de ingreso: " . $_POST['fecha_ingreso']. "\n";
    $message .= "Oficina/Sección: " . $_POST['oficina_seccion']. "\n";
    $message .= "División: " . $_POST['division']. "\n";
    $message .= "Departamento: " . $_POST['departamento']. "\n";
    $message .= "Domicilio Laboral: " . $_POST['domicilio_laboral']. "\n";
    $message .= "Teléfono Laboral: " . $_POST['telefono_laboral']. "\n";
    $message .= "Email: " . $_POST['email']. "\n";
    $message .= "Familiar 1: " . $_POST['familia_1']. "\n";
    $message .= "Familiar 2: " . $_POST['familia_2']. "\n";
    $message .= "Familiar 3: " . $_POST['familia_3']. "\n";
    $message .= "Familiar 4: " . $_POST['familia_4']. "\n";
    $message .= "Familiar 5: " . $_POST['familia_5']. "\n";
    $message .= "\n\n";
    $message .= "Muchas gracias. Te contactaremos a la brevedad.\n\n";

    $headers = "From:contacto@unidosaefip.org";
    mail($to,$subject,$message,$headers);
    mail($from,$subject,$message,$headers);

    echo '<html><head><meta http-equiv="refresh" content="5; url=file:///android_asset/4.html" /></head><body>';
    echo 'Email enviado.<br />Muchas gracias, ' . trim($name) . '.<br />Te contactaremos a la brevedad.';
    echo '</body></html>';
}
?>