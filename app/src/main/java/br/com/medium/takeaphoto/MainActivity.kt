package br.com.medium.takeaphoto

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

/**
 * Classe principal da aplicação.
 *
 * @author Augusto Santos
 * @version 1.0
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 100
    }

    @JvmField @BindView(R.id.imgv_photo)
    var imgvPhoto: SimpleDraweeView? = null

    @JvmField @BindView(R.id.fab_capture)
    var fabCapturePhoto: FloatingActionButton? = null

    private var currentPhotoPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        fabCapturePhoto?.setOnClickListener { validatePermissions()}
    }

    /**
     * Delega as operações referentes a permissões do uso da câmera
     * e acesso ao armazenamento interno a lib EasyPermissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions,
                grantResults, this)
    }

    /**
     * Realiza o processo de validação da permissões necessárias para
     * o app, verificando a necessidade de requeri-las ao usuario.
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS_CODE)
    private fun validatePermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (EasyPermissions.hasPermissions(this, *permissions)){
            launchCamera()
        } else {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.camera_request_rationale),
                    REQUEST_PERMISSIONS_CODE, *permissions)
        }
    }

    /**
     * Executa o aplicativo padrão de câmera e salva a foto tirada.
     */
    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager) != null) {
            currentPhotoPath = fileUri.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, REQUEST_PERMISSIONS_CODE)
        }
    }

    /**
     * Trata o processo de abertura da Activity verificando se ele
     * ocorreu devido a conclusao da fotografia.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PERMISSIONS_CODE) {
            processCapturedPhoto()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Processa a imagem registrada, gerando uma miniatura
     * para visualização.
     */
    private fun processCapturedPhoto() {
        val cursor = contentResolver.query(Uri.parse(currentPhotoPath),
                Array(1) {MediaStore.Images.ImageColumns.DATA},
                null, null, null)
        cursor.moveToFirst()
        val photoPath = cursor.getString(0)
        cursor.close()
        val file = File(photoPath)
        val uri = Uri.fromFile(file)
        val height = resources.getDimensionPixelSize(R.dimen.photo_height)
        val width = resources.getDimensionPixelSize(R.dimen.photo_width)
        val request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(ResizeOptions(width, height))
                .build()
        val controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imgvPhoto?.controller)
                .setImageRequest(request)
                .build()
        imgvPhoto?.controller = controller
    }

}
