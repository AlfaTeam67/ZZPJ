import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Button } from '@/components/ui/button';

describe('Button Component', () => {
  describe('Rendering', () => {
    it('should render button with text content', () => {
      render(<Button>Click me</Button>);
      expect(screen.getByRole('button', { name: /click me/i })).toBeInTheDocument();
    });

    it('should render button with custom className', () => {
      const { container } = render(<Button className="custom-class">Test</Button>);
      const button = container.querySelector('button');
      expect(button).toHaveClass('custom-class');
    });

    it('should render button with children as React node', () => {
      render(
        <Button>
          <span data-testid="icon">🎯</span>
          Action
        </Button>
      );
      expect(screen.getByTestId('icon')).toBeInTheDocument();
      expect(screen.getByRole('button')).toBeInTheDocument();
    });
  });

  describe('Variants', () => {
    it('should render button with default variant', () => {
      const { container } = render(<Button>Default</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('bg-primary');
    });

    it('should render button with outline variant', () => {
      const { container } = render(<Button variant="outline">Outline</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('bg-input');
    });

    it('should render button with ghost variant', () => {
      const { container } = render(<Button variant="ghost">Ghost</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('hover:bg-muted');
    });

    it('should render button with destructive variant', () => {
      const { container } = render(<Button variant="destructive">Delete</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('destructive');
    });

    it('should render button with link variant', () => {
      const { container } = render(<Button variant="link">Link</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('text-primary');
    });
  });

  describe('Sizes', () => {
    it('should render button with default size', () => {
      const { container } = render(<Button size="default">Default</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('h-9');
    });

    it('should render button with small size', () => {
      const { container } = render(<Button size="sm">Small</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('h-8');
    });

    it('should render button with large size', () => {
      const { container } = render(<Button size="lg">Large</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('h-10');
    });

    it('should render button with icon size', () => {
      const { container } = render(<Button size="icon">+</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('size-9');
    });
  });

  describe('Disabled State', () => {
    it('should render disabled button', () => {
      render(<Button disabled>Disabled</Button>);
      const button = screen.getByRole('button', { name: /disabled/i });
      expect(button).toBeDisabled();
    });

    it('should have opacity-50 when disabled', () => {
      const { container } = render(<Button disabled>Disabled</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('disabled:opacity-50');
    });

    it('should have pointer-events-none when disabled', () => {
      const { container } = render(<Button disabled>Disabled</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('disabled:pointer-events-none');
    });

    it('should not trigger onClick when disabled', async () => {
      const user = userEvent.setup();
      const handleClick = vi.fn();
      render(
        <Button disabled onClick={handleClick}>
          Disabled
        </Button>
      );
      const button = screen.getByRole('button');
      await user.click(button);
      expect(handleClick).not.toHaveBeenCalled();
    });
  });

  describe('Click Handler', () => {
    it('should call onClick handler when clicked', async () => {
      const user = userEvent.setup();
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Click</Button>);
      const button = screen.getByRole('button');
      await user.click(button);
      expect(handleClick).toHaveBeenCalledTimes(1);
    });

    it('should call onClick with mouse event', async () => {
      const user = userEvent.setup();
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Click</Button>);
      const button = screen.getByRole('button');
      await user.click(button);
      expect(handleClick).toHaveBeenCalledWith(expect.objectContaining({
        type: 'click',
      }));
    });

    it('should handle multiple clicks', async () => {
      const user = userEvent.setup();
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Click</Button>);
      const button = screen.getByRole('button');
      await user.click(button);
      await user.click(button);
      await user.click(button);
      expect(handleClick).toHaveBeenCalledTimes(3);
    });
  });

  describe('Accessibility', () => {
    it('should have proper focus styles', () => {
      const { container } = render(<Button>Focus</Button>);
      const button = container.querySelector('button');
      expect(button?.className).toContain('focus-visible:ring');
    });

    it('should support aria attributes', () => {
      render(<Button aria-label="Custom label">Button</Button>);
      expect(screen.getByLabelText('Custom label')).toBeInTheDocument();
    });

    it('should be keyboard accessible', async () => {
      const user = userEvent.setup();
      const handleClick = vi.fn();
      render(<Button onClick={handleClick}>Keyboard</Button>);
      const button = screen.getByRole('button');
      button.focus();
      expect(button).toHaveFocus();
      await user.keyboard('{Enter}');
      expect(handleClick).toHaveBeenCalled();
    });

    it('should support type attribute for form submission', () => {
      const { container } = render(<Button type="submit">Submit</Button>);
      const button = container.querySelector('button');
      expect(button).toHaveAttribute('type', 'submit');
    });
  });

  describe('Edge Cases', () => {
    it('should render with empty children', () => {
      const { container } = render(<Button></Button>);
      const button = container.querySelector('button');
      expect(button).toBeInTheDocument();
    });

    it('should handle long text content', () => {
      render(<Button>This is a very long button text that might wrap</Button>);
      expect(
        screen.getByRole('button', { name: /very long button text/i })
      ).toBeInTheDocument();
    });

    it('should apply custom className alongside variant classes', () => {
      const { container } = render(
        <Button variant="default" className="custom">
          Styled
        </Button>
      );
      const button = container.querySelector('button');
      expect(button?.className).toContain('custom');
    });
  });
});
