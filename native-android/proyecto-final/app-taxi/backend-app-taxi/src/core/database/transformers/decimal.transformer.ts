import { ValueTransformer } from "typeorm";

export const decimalTransformer: ValueTransformer = {
    to: (value?: number | null) => value,
    from: (value?: string | null) =>
        value === null || value === undefined ? null : Number(value),
};
