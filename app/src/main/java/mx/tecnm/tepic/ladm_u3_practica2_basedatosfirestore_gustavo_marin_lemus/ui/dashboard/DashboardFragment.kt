package mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_gustavo_marin_lemus.ui.dashboard

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import mx.tecnm.tepic.ladm_u3_practica2_basedatosfirestore_gustavo_marin_lemus.databinding.FragmentDashboardBinding
import java.lang.NullPointerException

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    val baseRemota= FirebaseFirestore.getInstance()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var listaidGlobal = ArrayList<String>()
    private var listaidGlobal2 = ArrayList<String>()
    private var idRequerido = ""
    private var idRequerido2 = ""
    private var descripcion = ""
    private var division = ""
    private var cantemp = 1
    private var modoDivDesc = "Descripcion"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        FirebaseFirestore.getInstance().collection("area")
            .addSnapshotListener { query, error ->
                if (error != null) {
                    //ERROR
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                listaidGlobal.clear()
                val llenar = ArrayList<String>()
                for (documento in query!!) {
                    var cadena = "${documento.getString("descripcion")}"
                    listaidGlobal.add(documento.id)
                    llenar.add(cadena)
                }

                binding.listaArea.adapter =
                    ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, llenar)
                binding.listaArea.setOnItemClickListener { adapterView, view, i, l ->
                    idRequerido = listaidGlobal.get(i).toString()
                    baseRemota.collection("area").document(idRequerido).get()
                        .addOnSuccessListener {
                            descripcion = it.getString("descripcion").toString()
                            division = it.getString("division").toString()
                            cantemp = it.getLong("cantidad_empleados").toString().toInt()
                        }

                }
            }

        FirebaseFirestore.getInstance().collection("subdepartamento")
            .addSnapshotListener { query, error ->
                if (error != null) {
                    //ERROR
                    AlertDialog.Builder(requireContext())
                        .setMessage(error.message)
                        .show()
                    return@addSnapshotListener
                }
                listaidGlobal2.clear()
                val llenar = ArrayList<String>()
                for (documento in query!!) {
                    var cadena = "${documento.getString("idedificio")}"
                    listaidGlobal2.add(documento.id)
                    llenar.add(cadena)
                }
                try {
                    binding.listaDepate.adapter =
                        ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, llenar)
                    binding.listaDepate.setOnItemClickListener { adapterView, view, i, l ->
                        idRequerido2 = listaidGlobal2.get(i).toString()
                        baseRemota.collection("subdepartamento").document(idRequerido2).get()
                            .addOnSuccessListener {
                                binding.ediEdicio.setText(it.getString("idedificio"))
                                binding.ediPiso.setText(it.getString("piso").toString())
                                binding.desc.setText("Descripción: ${it.getString("descripcion")}")
                                binding.divi.setText("División: ${it.getString("division")}")
                                binding.empleados.setText("Cantidad Empleados: ${it.getLong("cantidad_empleados")}")
                            }
                    }
                }catch (err:NullPointerException){

                }
            }
            binding.btnAgregar.setOnClickListener {
                agregarSubdepartamento()
                binding.desc.setText("")
                binding.divi.setText("")
                binding.empleados.setText("")
            }
            binding.btnActualiza.setOnClickListener {
                baseRemota.collection("subdepartamento").document(idRequerido2).
                update("idedificio",binding.ediEdicio.text.toString(),
                    "piso",binding.ediPiso.text.toString(),
                "descripcion",descripcion,"division",division,"cantidad_empleados",cantemp)
                binding.ediEdicio.setText("")
                binding.ediPiso.setText("")
                binding.desc.setText("")
                binding.divi.setText("")
                binding.empleados.setText("")
                Toast.makeText(requireContext(), "SE REALIZO LA ACTUALIZACIÓN CORRECTAMENTE", Toast.LENGTH_LONG).show()
            }
        binding.btneliminar.setOnClickListener {
            baseRemota.collection("subdepartamento").document(idRequerido2).delete()
                .addOnSuccessListener {
                    binding.ediEdicio.setText("")
                    binding.ediPiso.setText("")
                    binding.desc.setText("")
                    binding.divi.setText("")
                    binding.empleados.setText("")
                    Toast.makeText(requireContext(), "SE REALIZO LA ELIMINACIÓN CORRECTAMENTE", Toast.LENGTH_LONG).show()
                }
        }
        binding.btnBuscar.setOnClickListener {
            if(binding.ediEdicio.text.toString().equals("")){
                if(binding.descEs.text.toString().equals("")){
                    if (binding.divii.text.toString().equals("")){
                        var consulta=baseRemota.collection("subdepartamento")
                        realizarBusqueda(consulta)
                        Toast.makeText(requireContext(), "MOSTRANDO TODOS LOS DATOS", Toast.LENGTH_LONG).show()
                    }else{
                        var consulta=baseRemota.collection("subdepartamento").whereEqualTo("division",binding.divii.text.toString())
                        realizarBusqueda(consulta)
                        Toast.makeText(requireContext(), "MOSTRANDO DATOS POR DIVISIÓN", Toast.LENGTH_LONG).show()
                    }
                }else{
                    var consulta=baseRemota.collection("subdepartamento").whereEqualTo("descripcion",binding.descEs.text.toString())
                    realizarBusqueda(consulta)
                    Toast.makeText(requireContext(), "MOSTRANDO DATOS POR DESCRIPCIÓN", Toast.LENGTH_LONG).show()
                }
            }else{
                var consulta=baseRemota.collection("subdepartamento").whereEqualTo("idedificio",binding.ediEdicio.text.toString())
                realizarBusqueda(consulta)
                Toast.makeText(requireContext(), "MOSTRANDO DATOS POR EDIFICIO", Toast.LENGTH_LONG).show()
            }
        }
        binding.btndesdiv.setOnClickListener {
            if (modoDivDesc.equals("Descripción")){
                var consulta=baseRemota.collection("area")
                realizarBusqueda2(consulta,0)
                modoDivDesc = "División"
                binding.btndesdiv.setText("Descripción")
            }else{
                var consulta=baseRemota.collection("area")
                realizarBusqueda2(consulta,1)
                modoDivDesc = "Descripción"
                binding.btndesdiv.setText("División")
            }
        }

        return root
    }
    fun realizarBusqueda(queryy : Query) {
        queryy.addSnapshotListener { query, error ->
            if (error != null) {
                //ERROR
                AlertDialog.Builder(requireContext())
                    .setMessage(error.message)
                    .show()
                return@addSnapshotListener
            }
            listaidGlobal2.clear()
            val llenar = ArrayList<String>()
            for (documento in query!!) {
                    var cadena = "${documento.getString("idedificio")}"
                    llenar.add(cadena)
                    listaidGlobal2.add(documento.id)
            }
            binding.listaDepate.adapter =
                ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, llenar)
        }
    }
    fun realizarBusqueda2(queryy : Query,i : Int) {
        queryy.addSnapshotListener { query, error ->
            if (error != null) {
                //ERROR
                AlertDialog.Builder(requireContext())
                    .setMessage(error.message)
                    .show()
                return@addSnapshotListener
            }
            listaidGlobal.clear()
            val llenar = ArrayList<String>()
            for (documento in query!!) {
                if (i == 0) {
                    var cadena = "${documento.getString("division")}"
                    llenar.add(cadena)
                    listaidGlobal.add(documento.id)
                } else {
                    var cadena = "${documento.getString("descripcion")}"
                    llenar.add(cadena)
                    listaidGlobal.add(documento.id)
                }
            }
            binding.listaArea.adapter =
                ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, llenar)
        }
    }
    private fun agregarSubdepartamento(){
        if(binding.ediEdicio.text.toString().equals("")||binding.ediPiso.text.toString().equals("")){
            Toast.makeText(requireContext(), "INGRESE LOS DATOS REQUERIDOS", Toast.LENGTH_LONG).show()
        }else{
            val insertarDatos = hashMapOf(
                "idedificio" to binding.ediEdicio.text.toString(),
                "piso" to binding.ediPiso.text.toString(),
                "descripcion" to descripcion,
                "division" to division,
                "cantidad_empleados" to cantemp)
            baseRemota.collection("subdepartamento").add(insertarDatos)
                .addOnSuccessListener {
                    //SE REALIZO LA OPERACIÓN DE MANERA ÉXITOSA
                    Toast.makeText(requireContext(), "SE AGREGÓ DE MANERA ÉXITOSA EL SUBDEPARTAMENTO", Toast.LENGTH_LONG).show()
                    binding.ediEdicio.setText("")
                    binding.ediPiso.setText("")
                }
                .addOnFailureListener {
                    // HUBO UN FALLO
                    AlertDialog.Builder(requireContext())
                        .setMessage(it.message)
                        .show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}