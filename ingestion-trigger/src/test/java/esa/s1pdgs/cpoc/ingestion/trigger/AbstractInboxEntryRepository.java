package esa.s1pdgs.cpoc.ingestion.trigger;

import java.util.Collections;
import java.util.Optional;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;

public abstract class AbstractInboxEntryRepository implements InboxEntryRepository {

	@Override
	public <S extends InboxEntry> S save(final S entity) {
		return entity;
	}

	@Override
	public <S extends InboxEntry> Iterable<S> saveAll(final Iterable<S> entities) {
		return entities;
	}

	@Override
	public Optional<InboxEntry> findById(final Long id) {
		return Optional.empty();
	}

	@Override
	public boolean existsById(final Long id) {
		return false;
	}

	@Override
	public Iterable<InboxEntry> findAll() {
		return Collections.emptyList();
	}

	@Override
	public Iterable<InboxEntry> findAllById(final Iterable<Long> ids) {
		return Collections.emptyList();
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public void deleteById(final Long id) {	 }

	@Override
	public void delete(final InboxEntry entity) {}

	@Override
	public void deleteAll(final Iterable<? extends InboxEntry> entities) {}

	@Override
	public void deleteAll() {}
}
