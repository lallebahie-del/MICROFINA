package com.microfina.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * GarantieDocument — pièce justificative (preuve) associée à une garantie.
 *
 * Stockage: metadata en base + contenu sur disque (cf. backend GarantieDocumentStorageService).
 */
@Entity
@Table(name = "garantie_document")
@DynamicUpdate
public class GarantieDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "IDGARANTIE",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_garantie_document_Garantie")
    )
    private Garantie garantie;

    @Size(max = 255)
    @Column(name = "FILENAME", length = 255, nullable = false)
    private String filename;

    @Size(max = 100)
    @Column(name = "CONTENT_TYPE", length = 100, nullable = false)
    private String contentType;

    @Column(name = "SIZE_BYTES", nullable = false)
    private Long sizeBytes;

    @Size(max = 500)
    @Column(name = "STORAGE_PATH", length = 500, nullable = false)
    private String storagePath;

    @Size(max = 100)
    @Column(name = "UPLOADED_BY", length = 100)
    private String uploadedBy;

    @Column(name = "UPLOADED_AT")
    private LocalDateTime uploadedAt;

    @Version
    @Column(name = "VERSION")
    private Integer version = 0;

    public GarantieDocument() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Garantie getGarantie() { return garantie; }
    public void setGarantie(Garantie garantie) { this.garantie = garantie; }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    @Override
    public String toString() {
        return "GarantieDocument(id=" + id + ", filename=" + filename + ")";
    }
}

