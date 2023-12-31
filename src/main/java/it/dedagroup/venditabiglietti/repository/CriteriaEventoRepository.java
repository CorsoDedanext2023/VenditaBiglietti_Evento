package it.dedagroup.venditabiglietti.repository;

import it.dedagroup.venditabiglietti.dto.request.FiltraEventoDTORequest;
import it.dedagroup.venditabiglietti.model.Evento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CriteriaEventoRepository {
    @Autowired
    private EntityManager manager;

    public List<Evento> filtraEventi(FiltraEventoDTORequest request) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
        Root<Evento> root = query.from(Evento.class);
        List<Predicate> predicate = new ArrayList<>();
        if (request.getData() != null) {
            predicate.add(builder.greaterThanOrEqualTo(root.get("data"), request.getData()));
        }
        if(request.getDescrizione() != null) predicate.add(builder.like(builder.lower(root.get("descrizione")), "%"+ request.getDescrizione().toLowerCase()+"%"));
        predicate.add(builder.equal(root.get("isCancellato"), false));
        Predicate[] predicateArray = predicate.toArray(new Predicate[predicate.size()]);
        query.where(predicateArray);
        List<Tuple> list = manager.createQuery(query).getResultList();
        return list.stream().map(t -> t.get(0, Evento.class))
                .sorted(Comparator.comparing(Evento::getData).thenComparing(Evento::getOra))
                .toList();
    }

    public List<Evento> cercaEventi(Evento ev) {
        CriteriaBuilder cb = manager.getCriteriaBuilder();
        CriteriaQuery<Evento> cq = cb.createQuery(Evento.class);
        Root<Evento> evento = cq.from(Evento.class);

        Predicate descrizionePredicate = cb.equal(cb.lower(evento.get("descrizione")), ev.getDescrizione().toLowerCase());
        Predicate luogoPredicate = cb.and(cb.equal(evento.get("idLuogo"), ev.getIdLuogo()), cb.equal(evento.get("data"), ev.getData()));
        Predicate manifestazionePredicate = cb.and(cb.equal(evento.get("idManifestazione"), ev.getIdManifestazione()), cb.equal(evento.get("data"), ev.getData()));

        cq.select(evento).where(cb.or(descrizionePredicate, luogoPredicate, manifestazionePredicate));

        return manager.createQuery(cq).getResultList();
    }

}
