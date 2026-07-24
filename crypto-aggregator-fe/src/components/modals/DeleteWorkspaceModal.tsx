import {useState} from 'react';
import {Trash2} from 'lucide-react';
import Modal from '../ui/Modal';
import {Button} from '../ui';

type Props = {
  isOpen: boolean;
  onClose: () => void;
  workspaceName: string;
  onConfirm: () => Promise<void>;
};

export default function DeleteWorkspaceModal({isOpen, onClose, workspaceName, onConfirm}: Props) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleConfirm = async () => {
    setIsSubmitting(true);
    setError('');
    try {
      await onConfirm();
      onClose();
    } catch {
      setError('Failed to delete workspace.');
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <div className="bg-[#0b0e14] border border-white/10 rounded-xl shadow-2xl w-full max-w-md p-5 mx-auto">
        <div className="flex items-center gap-2 mb-2">
          <Trash2 size={18} className="text-[#f6465d]"/>
          <h2 className="text-base font-semibold text-zinc-50">Delete Workspace</h2>
        </div>
        <p className="text-sm text-zinc-400 mb-5">
          Are you sure you want to delete <span className="text-zinc-50 font-medium">"{workspaceName}"</span> and all its charts? This action cannot be undone.
        </p>

        {error && <p className="text-xs text-[#f6465d] mb-3">{error}</p>}

        <div className="flex items-center justify-end gap-2">
          <Button
            type="button"
            variant="secondary"
            size="sm"
            onClick={onClose}
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button
            type="button"
            variant="danger"
            size="sm"
            onClick={handleConfirm}
            isLoading={isSubmitting}
          >
            Delete
          </Button>
        </div>
      </div>
    </Modal>
  );
}
