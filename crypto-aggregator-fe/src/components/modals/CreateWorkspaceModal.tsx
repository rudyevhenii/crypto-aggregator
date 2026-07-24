import React, {useEffect, useRef, useState} from 'react';
import {FolderPlus} from 'lucide-react';
import Modal from '../ui/Modal';
import {Input} from '../ui';
import {Button} from '../ui';

type Props = {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (name: string) => Promise<void>;
};

export default function CreateWorkspaceModal({isOpen, onClose, onConfirm}: Props) {
  const [name, setName] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (isOpen) {
      setName('');
      setError('');
      setIsSubmitting(false);
      const timer = setTimeout(() => {
        inputRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const trimmed = name.trim();
    if (!trimmed) {
      setError('Workspace name cannot be empty.');
      return;
    }
    setIsSubmitting(true);
    setError('');
    try {
      await onConfirm(trimmed);
      setName('');
      onClose();
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Failed to create workspace.');
      setIsSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <div className="bg-[#0b0e14] border border-white/10 rounded-xl shadow-2xl w-full max-w-md p-5 mx-auto">
        <div className="flex items-center gap-2 mb-4">
          <FolderPlus size={18} className="text-[#fcd535]"/>
          <h2 className="text-base font-semibold text-zinc-50">New Workspace</h2>
        </div>

        <form onSubmit={handleSubmit}>
          <Input
            ref={inputRef}
            label="Workspace Name"
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              if (error) setError('');
            }}
            placeholder="Enter workspace name"
            error={error}
            autoFocus
          />

          <div className="flex items-center justify-end gap-2 mt-5">
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
              type="submit"
              size="sm"
              isLoading={isSubmitting}
            >
              Create
            </Button>
          </div>
        </form>
      </div>
    </Modal>
  );
}
