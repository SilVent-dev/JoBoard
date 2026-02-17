package br.com.joboard.dominio.entidade;

import br.com.joboard.dominio.enums.TipoUsuarioEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name="tipos_usuario")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TipoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoUsuarioEnum nome;
}
