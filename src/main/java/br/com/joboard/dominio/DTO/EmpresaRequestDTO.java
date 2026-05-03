package br.com.joboard.dominio.DTO;

import br.com.joboard.dominio.enums.PorteEmpresaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record EmpresaRequestDTO(@NotBlank(message = "Nome da empresa é obrigatório")
                                @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
                                String nome,

                                @Size(max = 500, message = "URL do site deve ter no máximo 500 caracteres")
                                String site,

                                String localizacao,
                                PorteEmpresaEnum porte,
                                String setor,
                                String culturaObservacoes,
                                String contatoRh,
                                String contatoReferencia) {

}
